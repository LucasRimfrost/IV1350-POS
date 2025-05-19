package se.kth.iv1350.pos.startup;

import se.kth.iv1350.pos.controller.Controller;
import se.kth.iv1350.pos.integration.RegistryCreator;
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
        RegistryCreator creator = RegistryCreator.getInstance();
        Controller controller = new Controller(creator);

        View view = new View(controller);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                controller.close();
                System.out.println("Resources closed successfully.");
            } catch (Exception e) {
                System.err.println("Error during shutdown: " + e.getMessage());
            }
        }));

        view.runFakeExecution();
    }
}
