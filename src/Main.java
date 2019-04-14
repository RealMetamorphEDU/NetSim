import javafx.application.Platform;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main extends JFrame {

    private FXCreator creator;
    private boolean running;

    private Main() {
        super("Title");
        creator = new FXCreator(this);
        setContentPane(creator.create());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if (creator.canClose())
                    running = false;
            }

            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                System.exit(0);
            }
        });
        pack();
        setMinimumSize(getPreferredSize());
        setResizable(true);
        setLocationRelativeTo(null);
        running = true;
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.setVisible(true);
        main.run();
    }

    private void run() {
        Platform.runLater(() -> {
            creator.start();
        });
        Runnable runnable = new Runnable() {
            long lastTime;
            double delta = 0;

            @Override
            public void run() {
                lastTime = System.currentTimeMillis();
                creator.update(delta);
                delta = System.currentTimeMillis() - lastTime;
            }
        };
        while (running) {
            Platform.runLater(runnable);
            try {
                Thread.sleep(creator.getDelay());
            } catch (InterruptedException ignored) {
            }
        }
        Platform.runLater(() -> {
            creator.end();
            this.dispose();
        });
    }

}
