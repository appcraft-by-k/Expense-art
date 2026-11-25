package com.appcraftbyk.expense.art.domain.error

/**
 * Expected, user-recoverable failures raised by use cases. Messages are safe to show in the UI:
 * they carry no internal details. Anything else is a programming/data error and should surface
 * as a generic error state.
 */
sealed class DomainException(message: String) : Exception(message) {

    data object InvalidAmount : DomainException("Enter an amount greater than zero.")

    data object BlankName : DomainException("Enter a name.")

    data object NotFound : DomainException("That item no longer exists.")

    data class DuplicateName(val name: String) : DomainException("\"$name\" already exists.")

    data object SystemRowNotDeletable : DomainException("Built-in items can be archived, not deleted.")

    class ReassignmentRequired(val usageCount: Int) :
        DomainException("This item is used by $usageCount ${if (usageCount == 1) "entry" else "entries"}. Choose what to move them to.")

    data object InvalidReassignment : DomainException("Choose a different active item of the same type.")

    data object ArchivedTarget : DomainException("The selected item is archived.")

    data object CategoryKindMismatch : DomainException("That category belongs to a different ledger.")

    data object AlreadySettled : DomainException("This debt is already settled.")

    data class OverPayment(val outstandingMinor: Long) :
        DomainException("Repayment is more than the outstanding balance.")

    data class WithdrawalExceedsBalance(val balanceMinor: Long) :
        DomainException("You cannot withdraw more than the pot balance.")
}
