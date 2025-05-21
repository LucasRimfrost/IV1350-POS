package se.kth.iv1350.pos.startup;

import se.kth.iv1350.pos.controller.Controller;
import se.kth.iv1350.pos.integration.RegistryCreator;
import se.kth.iv1350.pos.view.TotalRevenueFileOutput;
import se.kth.iv1350.pos.view.TotalRevenueView;
import se.kth.iv1350.pos.view.View;

/**
 * Entry point for the POS application. Responsible for initializing
 * the controller, setting up observers, and starting the user interface.
 */
public class Main {
    /**
     * Launches the application. Initializes core components and connects the
     * controller to the view. Also sets up a shutdown hook to ensure all resources
     * are properly released when the application terminates.
     *
     * @param args Command line arguments. These are not used in this application.
     */
    public static void main(String[] args) {
        RegistryCreator creator = RegistryCreator.getInstance();
        Controller controller = new Controller(creator);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down, closing resources...");
            try {
                controller.close();
            } catch (Exception e) {
                System.err.println("Error during shutdown: " + e.getMessage());
            }
        }));

        controller.addSaleObserver(new TotalRevenueView());
        controller.addSaleObserver(new TotalRevenueFileOutput());

        View view = new View(controller);
        view.runFakeExecution();
    }
}
