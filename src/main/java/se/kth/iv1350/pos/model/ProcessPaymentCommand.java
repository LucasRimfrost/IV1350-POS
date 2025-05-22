// File: ProcessPaymentCommand.java (in se.kth.iv1350.pos.model package)
package se.kth.iv1350.pos.model;

import se.kth.iv1350.pos.util.Amount;

/**
 * A command that processes a cash payment by updating the cash register.
 * This command focuses specifically on the cash register update portion
 * of payment processing.
 */
public class ProcessPaymentCommand implements SaleCommand {
    private final Amount paymentAmount;
    private final CashRegister cashRegister;

    /**
     * Creates a new command to process a payment.
     *
     * @param paymentAmount The amount being paid by the customer.
     * @param cashRegister The cash register to be updated.
     */
    public ProcessPaymentCommand(Amount paymentAmount, CashRegister cashRegister) {
        this.paymentAmount = paymentAmount;
        this.cashRegister = cashRegister;
    }

    /**
     * Executes the payment processing by updating the cash register.
     */
    @Override
    public void execute() {
        CashPayment payment = new CashPayment(paymentAmount);
        cashRegister.addPayment(payment);
    }

    /**
     * Returns a description of this payment command.
     */
    @Override
    public String getDescription() {
        return "Process payment of " + paymentAmount;
    }
}
