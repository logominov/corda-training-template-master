package net.corda.training.state

import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.training.contract.IOUContract
import java.util.*

/**
 * This is where you'll add the definition of your state object. Look at the unit tests in [IOUStateTests] for
 * instructions on how to complete the [IOUState] class.
 *
 * Remove the "val data: String = "data" property before starting the [IOUState] tasks.
 */
@BelongsToContract(IOUContract::class)
data class IOUState(val amount: Amount<Currency>, val lender: Party, val borrower: Party,
                    val paid: Amount<Currency> = Amount(0, amount.token),
                    override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {
    override val participants: List<Party> get() = listOf(lender, borrower)
    fun pay(amount: Amount<Currency>): IOUState = copy(paid = this.paid + amount)
    fun withNewLender (newLender: Party) : IOUState = copy(lender = newLender)
}


/**
 * Task 11.
 * TODO: Add a helper method called [withNewLender] that can be called from an [IOUState] to change the IOU's lender.
 */