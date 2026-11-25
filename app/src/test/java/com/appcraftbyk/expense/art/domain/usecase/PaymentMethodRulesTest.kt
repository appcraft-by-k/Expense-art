package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.testing.FakePaymentMethodRepository
import com.appcraftbyk.expense.art.testing.FakeTransactionRunner
import com.appcraftbyk.expense.art.testing.paymentMethodFixture
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/** Invariant 11: payment-method rules mirror categories. */
class PaymentMethodRulesTest {

    private val paymentMethods = FakePaymentMethodRepository()
    private val runner = FakeTransactionRunner()
    private val clock = AppClock(
        Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneId.of("Asia/Kolkata"))
    )
    private val deletePaymentMethod = DeletePaymentMethod(paymentMethods, runner, clock)
    private val savePaymentMethod = SavePaymentMethod(paymentMethods)

    @Before
    fun setUp() {
        paymentMethods.seed(
            paymentMethodFixture(id = 1, name = "UPI", isSystem = true, uid = "sys-upi")
        )
        paymentMethods.seed(paymentMethodFixture(id = 2, name = "Wallet"))
        paymentMethods.seed(paymentMethodFixture(id = 3, name = "Archived", isArchived = true))
    }

    @Test
    fun systemMethod_cannotBeDeleted() {
        assertThrows(DomainException.SystemRowNotDeletable::class.java) {
            runBlocking { deletePaymentMethod(paymentMethodId = 1) }
        }
        assertEquals("UPI", paymentMethods.item(1)?.name)
    }

    @Test
    fun unusedUserMethod_isDeleted() = runBlocking {
        deletePaymentMethod(paymentMethodId = 2)

        assertNull(paymentMethods.item(2))
    }

    @Test
    fun usedMethod_withoutTarget_requiresReassignment() {
        paymentMethods.setUsage(2, 4)

        val error = assertThrows(DomainException.ReassignmentRequired::class.java) {
            runBlocking { deletePaymentMethod(paymentMethodId = 2) }
        }

        assertEquals(4, error.usageCount)
        assertEquals("Wallet", paymentMethods.item(2)?.name)
        assertNull(paymentMethods.lastReassign)
    }

    @Test
    fun usedMethod_withArchivedTarget_isRejected() {
        paymentMethods.setUsage(2, 1)

        assertThrows(DomainException.InvalidReassignment::class.java) {
            runBlocking { deletePaymentMethod(paymentMethodId = 2, reassignToId = 3) }
        }
        assertEquals("Wallet", paymentMethods.item(2)?.name)
        assertNull(paymentMethods.lastReassign)
    }

    @Test
    fun usedMethod_withActiveTarget_reassignsAndDeletes() = runBlocking {
        paymentMethods.setUsage(2, 2)

        deletePaymentMethod(paymentMethodId = 2, reassignToId = 1)

        assertEquals(2L to 1L, paymentMethods.lastReassign)
        assertNull(paymentMethods.item(2))
    }

    @Test
    fun duplicateName_isRejectedCaseInsensitively() {
        assertThrows(DomainException.DuplicateName::class.java) {
            runBlocking {
                savePaymentMethod(
                    paymentMethodId = null,
                    name = "upi",
                    iconKey = "upi"
                )
            }
        }
        assertEquals(3, paymentMethods.all().size)
    }

    @Test
    fun edit_preservesUidAndSystemFlag() = runBlocking {
        savePaymentMethod(
            paymentMethodId = 1,
            name = "UPI / GPay",
            iconKey = "upi"
        )

        val updated = paymentMethods.item(1)!!
        assertEquals("UPI / GPay", updated.name)
        assertEquals("sys-upi", updated.uid)
        assertEquals(true, updated.isSystem)
    }

    @Test
    fun kindlessMethod_canBeArchived() = runBlocking {
        savePaymentMethod(
            paymentMethodId = 2,
            name = "Wallet",
            iconKey = "wallet",
            isArchived = true
        )

        assertEquals(true, paymentMethods.item(2)?.isArchived)
    }
}
