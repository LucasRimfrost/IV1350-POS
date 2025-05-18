package se.kth.iv1350.pos.startup;

import se.kth.iv1350.pos.controller.Controller;
import se.kth.iv1350.pos.integration.RegistryCreator;
import se.kth.iv1350.pos.integration.TotalRevenueFileOutput;
import se.kth.iv1350.pos.view.TotalRevenueView;
import se.kth.iv1350.pos.view.View;

/**
 * Contains the main method. Performs all startup of the application.
 */
public class Main {
    /**
     * The main method used to start the application.
     * Initializes all components, registers observers, and sets up a shutdown hook.
     *
     * @param args The application does not take any command line parameters.
     */
    public static void main(String[] args) {
        // Get singleton instance of RegistryCreator
        RegistryCreator creator = RegistryCreator.getInstance();
        Controller controller = new Controller(creator);

        // Create and add observers
        TotalRevenueView revenueView = new TotalRevenueView();
        TotalRevenueFileOutput revenueOutput = new TotalRevenueFileOutput();

        // Register observers with controller
        controller.addSaleObserver(revenueView);
        controller.addSaleObserver(revenueOutput);

        View view = new View(controller);

        // Set up shutdown hook for proper resource cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                controller.close();
                revenueOutput.close();
                System.out.println("Resources closed successfully.");
            } catch (Exception e) {
                System.err.println("Error during shutdown: " + e.getMessage());
            }
        }));

        view.runFakeExecution();
    }
}
