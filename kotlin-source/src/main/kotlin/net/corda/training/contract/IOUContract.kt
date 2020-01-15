package net.corda.training.contract

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import net.corda.finance.contracts.utils.sumObligations
import net.corda.training.state.IOUState
import net.corda.core.contracts.BelongsToContract as BelongsToContract1

/**
 * This is where you'll add the contract code which defines how the [IOUState] behaves. Look at the unit tests in
 * [IOUContractTests] for instructions on how to complete the [IOUContract] class.
 */
class IOUContract : Contract {
    companion object {
        @JvmStatic
        val IOU_CONTRACT_ID = "net.corda.training.contract.IOUContract"
    }

    /**
     * Add any commands required for this contract as classes within this interface.
     * It is useful to encapsulate your commands inside an interface, so you can use the [requireSingleCommand]
     * function to check for a number of commands which implement this interface.
     */
    interface Commands : CommandData {
        class Issue : TypeOnlyCommandData(), Commands
        class Transfer : TypeOnlyCommandData(), Commands
        class Settle : TypeOnlyCommandData(), Commands
        // Add commands here.
        // E.g
        // class DoSomething : TypeOnlyCommandData(), Commands
    }

    /**
     * The contract code for the [IOUContract].
     * The constraints are self documenting so don't require any additional explanation.
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<IOUContract.Commands>()

        when (command.value) {
            is Commands.Issue -> requireThat {
                "No inputs should be consumed when issuing an IOU." using (tx.inputs.isEmpty())
                "Only one output state should be created when issuing an IOU." using (tx.outputs.size == 1)
                val refIOU = tx.outputStates.single() as IOUState
                "A newly issued IOU must have a positive amount." using (refIOU.amount > Amount(0, refIOU.amount.token))
                "The lender and borrower cannot have the same identity." using (refIOU.lender != refIOU.borrower)
                "Both lender and borrower together only may sign IOU issue transaction." using (command.signers.toSet() == refIOU.participants.map { it.owningKey }.toSet())
            }
            is Commands.Transfer ->  requireThat {
                "An IOU transfer transaction should only consume one input state." using (tx.inputs.size == 1)
                "An IOU transfer transaction should only create one output state." using (tx.outputs.size == 1)
                val input = tx.inputStates.single() as IOUState
                val output = tx.outputStates.single() as IOUState
                "Only the lender property may change." using (input == output.withNewLender(input.lender))
                "The lender property must change in a transfer." using (input.lender != output.lender)
                "The borrower, old lender and new lender only must sign an IOU transfer transaction" using (command.signers.toSet() == (input.participants.map { it.owningKey }.toSet() `union` output.participants.map { it.owningKey }.toSet()))
            }
            is Commands.Settle -> requireThat {

                //val refIOU = tx.outputStates.single() as IOUState
            }
        }

    }
}


/**
 * Task 2.
 * For now, we only want to settle one IOU at once. We can use the [TransactionForContract.groupStates] function
 * to group the IOUs by their [linearId] property. We want to make sure there is only one group of input and output
 * IOUs.
 * TODO: Using [groupStates] add a constraint that checks for one group of input/output IOUs.
 * Hint:
 * - The [single] function enforces a single element in a list or throws an exception. The test checks the
 *   thrown exception so you do not have to define the exception message specifically in the code.
 * - The [groupStates] function takes two type parameters: the type of the state you wish to group by and the type
 *   of the grouping key used, in this case as you need to use the [linearId] and it is a [UniqueIdentifier].
 * - The [groupStates] also takes a lambda function which selects a property of the state to decide the groups.
 * - In Kotlin if the last argument of a function is a lambda, you can call it like this:
 *
 *       fun functionWithLambda() { it.property }
 *
 *   This is exactly how map / filter are used in Kotlin.
 */