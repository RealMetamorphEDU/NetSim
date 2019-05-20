import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import xmlparser.Document;
import xmlparser.XMLParser;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

class FXCreator {

    //Main
    private Main main;

    //FX
    private Label netLabel;
    private Label stateLabel;
    private Label ticksLabel;
    private Label bLabel;
    private Label gLabel;
    private Label rLabel;
    private Label delayLabel;
    private TextField countNodesField;
    private Slider bSlider;
    private Slider gSlider;
    private Slider rSlider;
    private Slider delaySlider;
    private Canvas canvas;
    private Button drawButton;
    private Button startButton;
    private Button graphButton;
    private Button loadButton;
    private Button saveButton;
    private CheckBox torus;

    //Logic
    private boolean working;
    private int count;
    private int count2;
    private Node[][] net;
    private double offset;
    private double stepI;
    private double stepJ;
    private static int nodes;
    private static int nodes_S;
    private static int nodes_I;
    private static int nodes_R;
    private double b;
    private double g;
    private double r;
    private Random random;
    private int delay;
    private int ticks;
    private LineChart lineChart;
    private XYChart.Series series_S;
    private XYChart.Series series_I;
    private XYChart.Series series_R;
    private boolean chartDrawing;
    private boolean choosePath;
    private Point2D startPoint;
    private MouseButton pressed;
    private Document document;


    FXCreator(Main main) {
        this.main = main;
    }

    JFXPanel create() {
        JFXPanel panel = new JFXPanel();
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("gui_general2.fxml"));
            netLabel = (Label) parent.lookup("#net_param_label");
            stateLabel = (Label) parent.lookup("#state");
            ticksLabel = (Label) parent.lookup("#tick_time");
            bLabel = (Label) parent.lookup("#var_b_label");
            gLabel = (Label) parent.lookup("#var_g_label");
            rLabel = (Label) parent.lookup("#var_r_label");
            delayLabel = (Label) parent.lookup("#var_delay_label");
            countNodesField = (TextField) parent.lookup("#count");
            canvas = (Canvas) parent.lookup("#canvas");
            drawButton = (Button) parent.lookup("#draw");
            bSlider = (Slider) parent.lookup("#var_b");
            gSlider = (Slider) parent.lookup("#var_g");
            rSlider = (Slider) parent.lookup("#var_r");
            delaySlider = (Slider) parent.lookup("#var_delay");
            startButton = (Button) parent.lookup("#btn_start");
            graphButton = (Button) parent.lookup("#btn_graph");
            loadButton = (Button) parent.lookup("#btn_load");
            saveButton = (Button) parent.lookup("#btn_save");
            torus = (CheckBox) parent.lookup("#use_torus");
            Separator vert = (Separator) parent.lookup("#sep_vert");
            Separator gor = (Separator) parent.lookup("#sep_gor");
            b = 0;
            g = 0;
            r = 0;
            ticks = 0;
            delay = 0;
            choosePath = false;
            startPoint = null;

            working = false;
            panel.setScene(new Scene(parent));
            vert.layoutXProperty().addListener((observable, oldValue, newValue) -> {
                canvas.setWidth(newValue.doubleValue() - 14);
                Platform.runLater(this::redrawNet);
            });
            gor.layoutYProperty().addListener((observable, oldValue, newValue) -> {
                canvas.setHeight(newValue.doubleValue() - 31);
                Platform.runLater(this::redrawNet);
            });
        } catch (IOException e) {
            System.exit(-1);
        }
        return panel;
    }

    boolean canClose() {
        return !working;
    }

    void start() {
        netLabel.setText("Сеть 0х0:");
        startButton.setDisable(true);
        graphButton.setDisable(true);
        offset = 30;
        countNodesField.setOnAction(event -> {
            drawButton.fire();
        });
        bSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            b = ((double) newValue.intValue() / 100) * 2 - 1;
            bLabel.setText("" + newValue.intValue());
        });
        gSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            g = ((double) newValue.intValue() / 100) * 2 - 1;
            gLabel.setText("" + newValue.intValue());
        });
        rSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            r = ((double) newValue.intValue() / 100) * 2 - 1;
            rLabel.setText("" + newValue.intValue());
        });
        delaySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            delay = newValue.intValue();
            delayLabel.setText("" + newValue.intValue());
        });
        bSlider.setValue(50);
        gSlider.setValue(50);
        rSlider.setValue(50);
        delaySlider.setValue(500);
        startButton.setOnAction(event -> {
            if (working) {
                working = false;
                startButton.setText("Старт");
                bSlider.setDisable(false);
                gSlider.setDisable(false);
                rSlider.setDisable(false);
                delaySlider.setDisable(false);
                drawButton.setDisable(false);
                saveButton.setDisable(false);
                loadButton.setDisable(false);
                stateLabel.setText(String.format("Состояние: Готово, N = %d, S = %d, I = %d, R = %d.", nodes, nodes_S, nodes_I, nodes_R));
            } else {
                random = new Random(System.currentTimeMillis());
                if (ticks == 0) {
                    series_S.getData().add(new XYChart.Data(0, nodes_S));
                    series_I.getData().add(new XYChart.Data(0, nodes_I));
                    series_R.getData().add(new XYChart.Data(0, nodes_R));
                }
                working = true;
                startButton.setText("Стоп");
                bSlider.setDisable(true);
                gSlider.setDisable(true);
                rSlider.setDisable(true);
                delaySlider.setDisable(true);
                drawButton.setDisable(true);
                graphButton.setDisable(false);
                saveButton.setDisable(true);
                loadButton.setDisable(true);
            }
        });
        drawButton.setOnAction(event -> {
            String text = countNodesField.getText().replaceAll("[^0-9*]", "");
            countNodesField.setText(text);
            String[] components = text.split("\\*");
            if (!components[0].isEmpty())
                count = Integer.parseInt(components[0]);
            if (components.length > 1) {

                count2 = Integer.parseInt(components[1]);
            } else
                count2 = count;
            if (components[0].isEmpty() && components.length > 1 && !components[1].isEmpty())
                count = count2;
            if (count > 100)
                count = 100;
            if (count2 > 100)
                count2 = 100;
            if (count < 3)
                count = 3;
            if (count2 < 3)
                count2 = 3;
            netLabel.setText(String.format("Сеть %dx%d:", count, count2));
            createAndDraw();
            nodes = count * count2;
            if (nodes > 0)
                startButton.setDisable(false);
            else
                startButton.setDisable(true);
            graphButton.setDisable(true);
            series_S = new XYChart.Series();
            series_S.setName("Восприимчивые узлы");
            series_I = new XYChart.Series();
            series_I.setName("Заражённые узлы");
            series_R = new XYChart.Series();
            series_R.setName("Имунные узлы");
            nodes_S = nodes;
            nodes_I = 0;
            nodes_R = 0;
            ticks = 0;
            stateLabel.setText(String.format("Состояние: Готово, N = %d, S = %d, I = %d, R = %d.", nodes, nodes_S, nodes_I, nodes_R));
            ticksLabel.setText(String.format("Шаг: %d", ticks));
            setTorus(torus.isSelected());
            saveButton.setDisable(false);
        });
        saveButton.setDisable(true);
        graphButton.setOnAction(event -> {
            createGraphic();
        });
        canvas.getGraphicsContext2D().beginPath();
        canvas.getGraphicsContext2D().setFill(Color.rgb(255, 255, 255));
        canvas.getGraphicsContext2D().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.getGraphicsContext2D().fill();
        canvas.getGraphicsContext2D().closePath();
        canvas.setOnMouseClicked(event -> {
            MouseButton pressed = event.getButton();
            if (event.getClickCount() == 2)
                redrawNet();
            if (ticks == 0 && (pressed == MouseButton.PRIMARY || pressed == MouseButton.SECONDARY)) {
                double iCoord = calcIndex(stepI, offset, event.getX());
                double jCoord = calcIndex(stepJ, offset, event.getY());

                int iOffset = Math.abs((int) (iCoord * 10)) % 10;
                int jOffset = Math.abs((int) (jCoord * 10)) % 10;

                int left = 1;
                int right = 9;

                if ((iOffset > left && iOffset < right) && (jOffset > left && jOffset < right))
                    return;

                if ((iOffset > left && iOffset < right) || (jOffset > left && jOffset < right)) {
                    Node n1;
                    Node n2;
                    if ((iOffset > left && iOffset < right)) {
                        int i1 = (int) Math.abs(Math.floor(iCoord));
                        int i2 = (int) Math.abs(Math.ceil(iCoord));
                        int j = (int) Math.abs(Math.round(jCoord));
                        if (iCoord < 0 || iCoord > count - 1) {
                            i1 = 0;
                            i2 = count - 1;
                            n1 = net[i1][j];
                            n2 = net[i2][j];
                            clear(canvas.getGraphicsContext2D(), 0, n1.y - 5, offset, 10);
                            clear(canvas.getGraphicsContext2D(), n2.x - 5, n1.y - 5, offset + 5, 10);
                        } else {
                            n1 = net[i1][j];
                            n2 = net[i2][j];
                            clear(canvas.getGraphicsContext2D(), n1.x - 5, n1.y - 5, stepI + 10, 10);
                        }
                    } else {
                        int i = (int) Math.abs(Math.round(iCoord));
                        int j1 = (int) Math.abs(Math.floor(jCoord));
                        int j2 = (int) Math.abs(Math.ceil(jCoord));
                        if (jCoord < 0 || jCoord > count2 - 1) {
                            j1 = 0;
                            j2 = count2 - 1;
                            n1 = net[i][j1];
                            n2 = net[i][j2];
                            clear(canvas.getGraphicsContext2D(), n1.x - 5, 0, 10, offset);
                            clear(canvas.getGraphicsContext2D(), n1.x - 5, n2.y - 5, 10, offset + 5);
                        } else {
                            n1 = net[i][j1];
                            n2 = net[i][j2];
                            clear(canvas.getGraphicsContext2D(), n1.x - 5, n1.y - 5, 10, stepJ + 10);
                        }
                    }
                    if (n1.hasNear(n2)) {
                        n1.removeNear(n2);
                        n2.removeNear(n1);
                    } else {
                        n1.addNear(n2);
                        n2.addNear(n1);
                    }
                    n1.draw(canvas.getGraphicsContext2D());
                    n2.draw(canvas.getGraphicsContext2D());
                }/* else {
                    int i = (int) (Math.round(iCoord));
                    int j = (int) (Math.round(jCoord));
                    if (i > -1 && j > -1 && i < count && j < count2) {
                        if (pressed == MouseButton.PRIMARY) {
                            net[i][j].nextState();
                        } else {
                            net[i][j].prevState();
                        }
                        net[i][j].draw(canvas.getGraphicsContext2D());
                    }
                    stateLabel.setText(String.format("Состояние: Готово, N = %d, S = %d, I = %d, R = %d.", nodes, nodes_S, nodes_I, nodes_R));
                }*/
            }
        });
        canvas.setOnMousePressed(event -> {
            pressed = event.getButton();
            if (ticks == 0 && (pressed == MouseButton.PRIMARY || pressed == MouseButton.SECONDARY)) {
                int iOffset = Math.abs((int) (calcIndex(stepI, offset, event.getX()) * 10)) % 10;
                int jOffset = Math.abs((int) (calcIndex(stepJ, offset, event.getY()) * 10)) % 10;

                int left = 1;
                int right = 9;

                if ((iOffset > left && iOffset < right) && (jOffset > left && jOffset < right))
                    return;

                startPoint = new Point2D(event.getX(), event.getY());
                choosePath = (iOffset > left && iOffset < right) || (jOffset > left && jOffset < right);
            }
        });
        canvas.setOnMouseReleased(event -> {
            if (startPoint != null) {
                double iCoord1 = calcIndex(stepI, offset, startPoint.getX());
                double jCoord1 = calcIndex(stepJ, offset, startPoint.getY());
                double iCoord2 = calcIndex(stepI, offset, event.getX());
                double jCoord2 = calcIndex(stepJ, offset, event.getY());

                if (choosePath) {
                    // TODO: 01.04.2019 DO IT!
                } else {
                    int i1 = (int) (Math.round(iCoord1));
                    int j1 = (int) (Math.round(jCoord1));
                    int i2 = (int) (Math.round(iCoord2));
                    int j2 = (int) (Math.round(jCoord2));

                    if (i1 > i2) {
                        int t = i2;
                        i2 = i1;
                        i1 = t;
                    }

                    if (j1 > j2) {
                        int t = j2;
                        j2 = j1;
                        j1 = t;
                    }

                    for (int i = i1; i <= i2; i++) {
                        for (int j = j1; j <= j2; j++) {
                            if (i > -1 && j > -1 && i < count && j < count2) {
                                clear(canvas.getGraphicsContext2D(), net[i][j].x - 5, net[i][j].y - 5, 10, 10);
                                if (event.isShiftDown()) {
                                    net[i][j].delNode();
                                } else if (pressed == MouseButton.PRIMARY) {
                                    net[i][j].nextState();
                                } else {
                                    net[i][j].prevState();
                                }
                                net[i][j].draw(canvas.getGraphicsContext2D());
                            }
                        }
                    }
                    stateLabel.setText(String.format("Состояние: Готово, N = %d, S = %d, I = %d, R = %d.", nodes, nodes_S, nodes_I, nodes_R));
                }
                startPoint = null;
            }
        });
        torus.setOnAction(event -> {
            setTorus(torus.isSelected());
        });
        saveButton.setOnAction(event -> {
            document = Document.createDocument();
            xmlparser.Node root = new xmlparser.Node("net_sim");
            document.appendChild(root);
            root.setAttribute("b", "" + (((b + 1) / 2) * 100));
            root.setAttribute("g", "" + (((g + 1) / 2) * 100));
            root.setAttribute("r", "" + (((r + 1) / 2) * 100));
            root.setAttribute("delay", "" + delay);
            root.setAttribute("torus", "" + torus.isSelected());
            root.setAttribute("steps", "" + ticks);
            xmlparser.Node root2 = new xmlparser.Node("net");
            root2.setAttribute("w", "" + count);
            root2.setAttribute("h", "" + count2);
            root.appendChild(root2);
            xmlparser.Node line;
            xmlparser.Node node;
            for (int i = 0; i < count; i++) {
                line = new xmlparser.Node("line");
                line.setAttribute("i", "" + i);
                for (int j = 0; j < count2; j++) {
                    node = new xmlparser.Node("node");
                    node.setAttribute("j", "" + j);
                    node.setAttribute("state", "" + net[i][j].state);
                    for (Node node1 : net[i][j].nears) {
                        xmlparser.Node near = new xmlparser.Node("near");
                        near.setAttribute("i", "" + node1.i);
                        near.setAttribute("j", "" + node1.j);
                        node.appendChild(near);
                    }
                    line.appendChild(node);
                }
                root2.appendChild(line);
            }
            root2 = new xmlparser.Node("graph");
            addSeries(root2, series_S.getData(), "s");
            addSeries(root2, series_I.getData(), "i");
            addSeries(root2, series_R.getData(), "r");
            root.appendChild(root2);
            try {
                XMLParser.save("net.save", document);
            } catch (IOException ignored) {
            }
        });

        loadButton.setOnAction(event -> {
            try {
                document = XMLParser.parse("net.save");
                xmlparser.Node root = document.getNodesByTagName("net_sim").get(0);
                bSlider.setValue(Float.parseFloat(root.getAttribute("b")));
                gSlider.setValue(Float.parseFloat(root.getAttribute("g")));
                rSlider.setValue(Float.parseFloat(root.getAttribute("r")));
                delay = Integer.parseInt(root.getAttribute("delay"));
                ticks = Integer.parseInt(root.getAttribute("steps"));
                torus.setSelected(Boolean.parseBoolean(root.getAttribute("torus")));
                xmlparser.Node root2 = root.getNodesByTagName("net").get(0);
                count = Integer.parseInt(root2.getAttribute("w"));
                count2 = Integer.parseInt(root2.getAttribute("h"));
                GraphicsContext context = canvas.getGraphicsContext2D();
                double startPos = offset;
                stepJ = (canvas.getHeight() - 2 * startPos) / (count2 - 1);
                stepI = (canvas.getWidth() - 2 * startPos) / (count - 1);
                net = new Node[count][count2];
                if (count != count2)
                    countNodesField.setText("" + count + "*" + count2);
                else
                    countNodesField.setText("" + count);
                clear(context, 0, 0, canvas.getWidth(), canvas.getHeight());
                nodes_S = 0;
                nodes_I = 0;
                nodes_R = 0;
                for (xmlparser.Node line : root2.getNodesByTagName("line")) {
                    int i = Integer.parseInt(line.getAttribute("i"));
                    for (xmlparser.Node node : line.getNodesByTagName("node")) {
                        int j = Integer.parseInt(node.getAttribute("j"));
                        byte state = Byte.parseByte(node.getAttribute("state"));
                        net[i][j] = new Node(i * stepI + startPos, j * stepJ + startPos, i, j, state, count - 1, count2 - 1, startPos);
                        switch (state) {
                            case Node.S:
                                nodes_S++;
                                break;
                            case Node.I:
                                nodes_I++;
                                break;
                            case Node.R:
                                nodes_R++;
                                break;
                        }
                    }
                }
                nodes = nodes_S + nodes_I + nodes_R;
                for (xmlparser.Node line : root2.getNodesByTagName("line")) {
                    int i = Integer.parseInt(line.getAttribute("i"));
                    for (xmlparser.Node node : line.getNodesByTagName("node")) {
                        int j = Integer.parseInt(node.getAttribute("j"));
                        for (xmlparser.Node near : node.getNodesByTagName("near")) {
                            int i1 = Integer.parseInt(near.getAttribute("i"));
                            int j1 = Integer.parseInt(near.getAttribute("j"));
                            net[i][j].addNear(net[i1][j1]);
                        }
                        net[i][j].draw(canvas.getGraphicsContext2D());
                    }
                }
                series_S = new XYChart.Series();
                series_S.setName("Восприимчивые узлы");
                series_I = new XYChart.Series();
                series_I.setName("Заражённые узлы");
                series_R = new XYChart.Series();
                series_R.setName("Имунные узлы");
                if (ticks > 0) {
                    graphButton.setDisable(false);
                    root2 = root.getNodesByTagName("graph").get(0);
                    getSeries(root2, series_S.getData(), "s");
                    getSeries(root2, series_I.getData(), "i");
                    getSeries(root2, series_R.getData(), "r");
                    graphButton.setDisable(false);
                }
                saveButton.setDisable(false);
                startButton.setDisable(false);
                stateLabel.setText(String.format("Состояние: Готово, N = %d, S = %d, I = %d, R = %d.", nodes, nodes_S, nodes_I, nodes_R));
            } catch (IOException ignored) {
            }
        });
        stateLabel.setText("Состояние: Пусто");
    }

    private void addSeries(xmlparser.Node root2, ObservableList<XYChart.Data> dataset, String tag) {
        if (dataset.isEmpty())
            return;
        xmlparser.Node series = new xmlparser.Node(tag);
        xmlparser.Node dat;
        for (XYChart.Data data : dataset) {
            dat = new xmlparser.Node("data");
            dat.setAttribute("tick", "" + data.getXValue());
            dat.setAttribute("count", "" + data.getYValue());
            series.appendChild(dat);
        }
        root2.appendChild(series);
    }

    private void getSeries(xmlparser.Node root2, ObservableList<XYChart.Data> dataset, String tag) {
        xmlparser.Node series = root2.getNodesByTagName(tag).get(0);
        for (xmlparser.Node dat : series.getNodesByTagName("data")) {
            dataset.add(new XYChart.Data(Integer.parseInt(dat.getAttribute("tick")), Integer.parseInt(dat.getAttribute("count"))));
        }
    }

    private void setTorus(boolean newValue) {
        if (newValue) {
            for (int i = 0; i < count; i++) {
                Node n1 = net[i][0];
                Node n2 = net[i][count2 - 1];
                if (!n1.hasNear(n2)) {
                    n1.addNear(n2);
                    n2.addNear(n1);
                    clear(canvas.getGraphicsContext2D(), n1.x - 5, 0, 10, offset);
                    clear(canvas.getGraphicsContext2D(), n1.x - 5, n2.y - 5, 10, offset + 5);
                    n1.draw(canvas.getGraphicsContext2D());
                    n2.draw(canvas.getGraphicsContext2D());
                }
            }
            for (int j = 0; j < count2; j++) {
                Node n1 = net[0][j];
                Node n2 = net[count - 1][j];
                if (!n1.hasNear(n2)) {
                    n1.addNear(n2);
                    n2.addNear(n1);
                    clear(canvas.getGraphicsContext2D(), 0, n1.y - 5, offset, 10);
                    clear(canvas.getGraphicsContext2D(), n2.x - 5, n1.y - 5, offset + 5, 10);
                    n1.draw(canvas.getGraphicsContext2D());
                    n2.draw(canvas.getGraphicsContext2D());
                }
            }
        } else {
            for (int i = 0; i < count; i++) {
                Node n1 = net[i][0];
                Node n2 = net[i][count2 - 1];
                if (n1.hasNear(n2)) {
                    n1.removeNear(n2);
                    n2.removeNear(n1);
                    clear(canvas.getGraphicsContext2D(), n1.x - 5, 0, 10, offset);
                    clear(canvas.getGraphicsContext2D(), n1.x - 5, n2.y - 5, 10, offset + 5);
                    n1.draw(canvas.getGraphicsContext2D());
                    n2.draw(canvas.getGraphicsContext2D());
                }
            }
            for (int j = 0; j < count2; j++) {
                Node n1 = net[0][j];
                Node n2 = net[count - 1][j];
                if (n1.hasNear(n2)) {
                    n1.removeNear(n2);
                    n2.removeNear(n1);
                    clear(canvas.getGraphicsContext2D(), 0, n1.y - 5, offset, 10);
                    clear(canvas.getGraphicsContext2D(), n2.x - 5, n1.y - 5, offset + 5, 10);
                    n1.draw(canvas.getGraphicsContext2D());
                    n2.draw(canvas.getGraphicsContext2D());
                }
            }
        }
    }

    private void clear(GraphicsContext context, double x, double y, double w, double h) {
        context.beginPath();
        context.setFill(Color.rgb(255, 255, 255));
        context.fillRect(x, y, w, h);
        context.closePath();
    }

    private void createAndDraw() {
        GraphicsContext context = canvas.getGraphicsContext2D();
        double startPos = offset;
        stepJ = (canvas.getHeight() - 2 * startPos) / (count2 - 1);
        stepI = (canvas.getWidth() - 2 * startPos) / (count - 1);
        net = new Node[count][count2];
        clear(context, 0, 0, canvas.getWidth(), canvas.getHeight());
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count2; j++) {
                net[i][j] = new Node(i * stepI + startPos, j * stepJ + startPos, i, j, Node.S, count - 1, count2 - 1, startPos);
            }
        }
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count2; j++) {
                if (i > 0)
                    net[i][j].addNear(net[i - 1][j]);
                if (j > 0)
                    net[i][j].addNear(net[i][j - 1]);
                if (i < count - 1)
                    net[i][j].addNear(net[i + 1][j]);
                if (j < count2 - 1)
                    net[i][j].addNear(net[i][j + 1]);
                net[i][j].draw(context);
            }
        }
    }

    private void redrawNet() {
        GraphicsContext context = canvas.getGraphicsContext2D();
        double startPos = offset;
        stepJ = (canvas.getHeight() - 2 * startPos) / (count2 - 1);
        stepI = (canvas.getWidth() - 2 * startPos) / (count - 1);
        clear(context, 0, 0, canvas.getWidth(), canvas.getHeight());
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count2; j++) {
                net[i][j].x = i * stepI + startPos;
                net[i][j].y = j * stepJ + startPos;
                net[i][j].draw(context);
            }
        }
    }

    private double calcIndex(double step, double offset, double value) {
        return (value - offset) / step;
    }

    void update(double delta) {
        if (working) {
            for (int i = 0; i < count; i++) {
                for (int j = 0; j < count2; j++) {
                    net[i][j].setStateLast();
                }
            }
            for (int i = 0; i < count; i++) {
                for (int j = 0; j < count2; j++) {
                    net[i][j].calculateNewState(b, g, r, random, canvas.getGraphicsContext2D());
                }
            }
            ticks++;
            series_S.getData().add(new XYChart.Data(ticks, nodes_S));
            series_I.getData().add(new XYChart.Data(ticks, nodes_I));
            series_R.getData().add(new XYChart.Data(ticks, nodes_R));
            ticksLabel.setText(String.format("Шаг: %d", ticks));
            if (chartDrawing) {
                ((NumberAxis) lineChart.getXAxis()).setUpperBound(ticks);
                ((NumberAxis) lineChart.getXAxis()).setTickUnit((double) ticks / 10);
            }
            stateLabel.setText(String.format("Состояние: Симуляция, N = %d, S = %d, I = %d, R = %d.", nodes, nodes_S, nodes_I, nodes_R));
        }
    }

    int getDelay() {
        return delay;
    }

    void end() {

    }

    private void createGraphic() {
        JFXPanel panel = new JFXPanel();
        NumberAxis xAxis = new NumberAxis("Шаги", 0, ticks, (double) ticks / 10);
        NumberAxis yAxis = new NumberAxis("Узлы", 0, nodes, (double) nodes / 15);
        lineChart = new LineChart(xAxis, yAxis);
        lineChart.getData().add(series_I);
        lineChart.getData().add(series_S);
        lineChart.getData().add(series_R);
        lineChart.setTitle("График количества узлов от времени");
        Group root = new Group(lineChart);
        panel.setScene(new Scene(root, 600, 400));
        JDialog dialog = new JDialog(main);
        dialog.setTitle("График");
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosed(e);
                chartDrawing = false;
            }
        });
        chartDrawing = true;
    }

    private static class Node {

        private double x;
        private double y;
        private int i;
        private int j;
        private ArrayList<Node> nears;
        private byte state;
        private byte stateLast;
        private String type;

        private int stepMaxI;
        private int stepI;

        static final byte S = 0;
        static final byte I = 1;
        static final byte R = 2;
        static final byte D = 3;
        private double offset;

        Node(double x, double y, int i, int j, byte state, int iMax, int jMax, double offset) {
            this.x = x;
            this.y = y;
            this.i = i;
            this.j = j;
            this.offset = offset;
            nears = new ArrayList<>();
            this.state = state;
            if (i == 0 && j == 0) {
                type = "left-up";
            } else if (i == iMax && j == 0) {
                type = "right-up";
            } else if (i == iMax && j == jMax) {
                type = "right-down";
            } else if (i == 0 && j == jMax) {
                type = "left-down";
            } else if (i == 0 && (j > 0 && j < jMax)) {
                type = "left";
            } else if (i == iMax && (j > 0 && j < jMax)) {
                type = "right";
            } else if ((i > 0 && i < iMax) && j == 0) {
                type = "up";
            } else if ((i > 0 && i < iMax) && j == jMax) {
                type = "down";
            } else {
                type = "into";
            }
        }

        void draw(GraphicsContext context) {
            if (state == D)
                return;
            drawLines(context);
            drawNode(context);
        }

        private void drawLines(GraphicsContext context) {
            if (state == D)
                return;
            context.setStroke(Color.rgb(0, 0, 0));
            for (Node node : nears) {
                if (node.state == D)
                    continue;
                context.beginPath();
                boolean classic = false;
                switch (type) {
                    case "left":
                        switch (node.type) {
                            case "right":
                                if (j == node.j) {
                                    context.moveTo(0, y);
                                }
                                break;
                            case "left":
                            case "left-up":
                            case "left-down":
                            case "into":
                                classic = true;
                                break;
                        }
                        break;
                    case "right":
                        switch (node.type) {
                            case "left":
                                if (j == node.j) {
                                    context.moveTo(x + offset, y);
                                }
                                break;
                            case "right":
                            case "right-up":
                            case "right-down":
                            case "into":
                                classic = true;
                                break;
                        }
                        break;
                    case "up":
                        switch (node.type) {
                            case "down":
                                if (i == node.i) {
                                    context.moveTo(x, 0);
                                }
                                break;
                            case "up":
                            case "left-up":
                            case "right-up":
                            case "into":
                                classic = true;
                                break;
                        }
                        break;
                    case "down":
                        switch (node.type) {
                            case "up":
                                if (i == node.i) {
                                    context.moveTo(x, y + offset);
                                }
                                break;
                            case "down":
                            case "left-down":
                            case "right-down":
                            case "into":
                                classic = true;
                                break;
                        }
                        break;
                    case "left-up":
                        switch (node.type) {
                            case "left-down":
                                context.moveTo(x, 0);
                                break;
                            case "right-up":
                                context.moveTo(0, y);
                                break;
                            case "left":
                            case "up":
                                classic = true;
                                break;
                        }
                        break;
                    case "left-down":
                        switch (node.type) {
                            case "left-up":
                                context.moveTo(x, y + offset);
                                break;
                            case "right-down":
                                context.moveTo(0, y);
                                break;
                            case "left":
                            case "down":
                                classic = true;
                                break;
                        }
                        break;
                    case "right-up":
                        switch (node.type) {
                            case "left-up":
                                context.moveTo(x + offset, y);
                                break;
                            case "right-down":
                                context.moveTo(x, 0);
                                break;
                            case "right":
                            case "up":
                                classic = true;
                                break;
                        }
                        break;
                    case "right-down":
                        switch (node.type) {
                            case "left-down":
                                context.moveTo(x + offset, y);
                                break;
                            case "right-up":
                                context.moveTo(x, y + offset);
                                break;
                            case "right":
                            case "down":
                                classic = true;
                                break;
                        }
                        break;
                    case "into":
                        classic = true;
                        break;
                }
                if (classic) {
                    context.moveTo(x, y);
                    context.lineTo(node.x, node.y);
                } else {
                    context.lineTo(x, y);
                }
                context.stroke();
                context.closePath();
                node.drawNode(context);
            }
        }

        void calculateNewState(double b, double g, double r, Random random, GraphicsContext context) {
            if (state == D)
                return;
            int typeS = 0;
            int typeI = 0;
            int typeR = 0;
            for (Node node : nears) {
                switch (node.stateLast) {
                    case S:
                        typeS++;
                        break;
                    case I:
                        typeI++;
                        break;
                    case R:
                        typeR++;
                        break;
                }
            }
            switch (stateLast) {
                case S:
                    double v = (double) typeI / (typeS + typeI + typeR);
                    v += b;
                    v = v > 1 ? 1 : (v < 0 ? 0 : v);
                    int chance = (int) Math.round(v * 100);
                    int rnd = random.nextInt(100);
                    if (rnd < chance) {
                        changeState(I);
                        stepMaxI = (int) (5 + Math.abs(v * 100) * rnd);
                        stepI = 0;
                        draw(context);
                    }
                    break;
                case I:
                    v = 1 - ((double) typeI / (typeS + typeI + typeR));
                    v += g;
                    v = v > 1 ? 1 : (v < 0 ? 0 : v);
                    stepI++;
                    chance = (int) Math.round(v * 100);
                    rnd = random.nextInt(100);
                    if (rnd < chance || stepI > stepMaxI) {
                        changeState(R);
                        draw(context);
                    }
                    break;
                case R:
                    v = 1 - ((double) typeR / (typeS + typeI + typeR));
                    v += r;
                    v = v > 1 ? 1 : (v < 0 ? 0 : v);
                    chance = (int) Math.round(v * 100);
                    rnd = random.nextInt(100);
                    if (rnd < chance) {
                        changeState(S);
                        draw(context);
                    }
                    break;
            }
        }

        private void drawNode(GraphicsContext context) {
            if (state == D)
                return;
            context.beginPath();
            switch (state) {
                case S:
                    context.setFill(Color.rgb(220, 250, 0));
                    break;
                case I:
                    context.setFill(Color.rgb(255, 5, 0));
                    break;
                case R:
                    context.setFill(Color.rgb(9, 128, 0));
                    break;
            }
            context.fillOval(x - 5, y - 5, 10, 10);
            context.fill();
            context.closePath();
        }

        void addNear(Node node) {
            if (!nears.contains(node))
                nears.add(node);
        }

        void removeNear(Node node) {
            nears.remove(node);
        }

        boolean hasNear(Node node) {
            return nears.contains(node);
        }

        private void changeState(byte state) {
            if (this.state == D)
                return;
            switch (this.state) {
                case S:
                    nodes_S--;
                    break;
                case I:
                    nodes_I--;
                    break;
                case R:
                    nodes_R--;
                    break;
            }
            this.state = state;
            if (this.state < 0)
                this.state = 2;
            if (this.state > 2)
                this.state = 0;
            switch (this.state) {
                case S:
                    nodes_S++;
                    break;
                case I:
                    nodes_I++;
                    break;
                case R:
                    nodes_R++;
                    break;
            }
        }

        void setStateLast() {
            stateLast = state;
        }

        byte getState() {
            return state;
        }

        void nextState() {
            changeState((byte) (getState() + 1));
        }

        void prevState() {
            changeState((byte) (getState() - 1));
        }

        void delNode() {
            if (this.state == D) {
                this.state = S;
                nodes++;
                nodes_S++;
            } else {
                switch (this.state) {
                    case S:
                        nodes_S--;
                        break;
                    case I:
                        nodes_I--;
                        break;
                    case R:
                        nodes_R--;
                        break;
                }
                this.state = D;
                nodes--;
            }
        }

    }
}
