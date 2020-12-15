package com.bsu.rfe.java.group10.lab5.CharnetskyVladimir.varC2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JPanel;

public class GraphicsDisplay extends JPanel{
    private ArrayList<Double[]> graphicsData;
    private ArrayList<Double[]> originalData;
    private int selectedMarker = -1;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    private double dx = 0;
    private double dy = 0;

    private double Dx = 0;
    private double Dy = 0;

    private double[][] viewport = new double[2][2];
    private ArrayList<double[][]> undoHistory = new ArrayList(5);
    private ArrayList<double[][]> undoHistory_2 = new ArrayList(5);
    private double scaleX;
    private double scaleY;
    private BasicStroke axisStroke;
    private BasicStroke gridStroke;
    private BasicStroke markerStroke;
    private BasicStroke selectionStroke;
    private Font axisFont;
    private Font labelsFont;
    private static DecimalFormat formatter = (DecimalFormat)NumberFormat.getInstance();
    private boolean scaleMode = false; //для приблиения
    private boolean changeMode = false; //для изменения значений графика

    private boolean changeView = false;// Для передвижения графика и всех его сотавляющих

    private double[] originalPoint = new double[2];

    private double[] originalPoint_2 = new double[2];

    private java.awt.geom.Rectangle2D.Double selectionRect = new java.awt.geom.Rectangle2D.Double();

    int a = 0;// Для осей

    public GraphicsDisplay() {
        this.setBackground(Color.WHITE);
        this.axisStroke = new BasicStroke(2.0F, 0, 0, 10.0F, (float[])null, 0.0F);
        this.gridStroke = new BasicStroke(1.0F, 0, 0, 10.0F, new float[]{4.0F, 4.0F}, 0.0F);
        this.markerStroke = new BasicStroke(1.0F, 0, 0, 10.0F, (float[])null, 0.0F);
        this.selectionStroke = new BasicStroke(1.0F, 0, 0, 10.0F, new float[]{10.0F, 10.0F}, 0.0F);
        this.axisFont = new Font("Serif", 1, 36);
        this.labelsFont = new Font("Serif", 0, 10);
        formatter.setMaximumFractionDigits(5);
        this.addMouseListener(new GraphicsDisplay.MouseHandler());
        this.addMouseMotionListener(new GraphicsDisplay.MouseMotionHandler());
    }

    public void displayGraphics(ArrayList<Double[]> graphicsData) {
        this.graphicsData = graphicsData;
        this.originalData = new ArrayList(graphicsData.size());
        Iterator var3 = graphicsData.iterator();

        while(var3.hasNext()) {
            Double[] point = (Double[])var3.next();
            Double[] newPoint = new Double[]{new Double(point[0]), new Double(point[1])};
            this.originalData.add(newPoint);
        }

        this.minX = ((Double[])graphicsData.get(0))[0];
        this.maxX = ((Double[])graphicsData.get(graphicsData.size() - 1))[0];
        this.minY = ((Double[])graphicsData.get(0))[1];
        this.maxY = this.minY;

        for(int i = 1; i < graphicsData.size(); ++i) {
            if (((Double[])graphicsData.get(i))[1] < this.minY) {
                this.minY = ((Double[])graphicsData.get(i))[1];
            }

            if (((Double[])graphicsData.get(i))[1] > this.maxY) {
                this.maxY = ((Double[])graphicsData.get(i))[1];
            }
        }

        this.zoomToRegion(this.minX, this.maxY, this.maxX, this.minY);
    }//Вызвав этот метод с начальными дынными нарисуем график

    public void zoomToRegion(double x1, double y1, double x2, double y2) {
        this.viewport[0][0] = x1;
        this.viewport[0][1] = y1;
        this.viewport[1][0] = x2;
        this.viewport[1][1] = y2;
        this.repaint();
    }//Установит диапазон значений графика вызовет зарисовку

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.scaleX = this.getSize().getWidth() / (this.viewport[1][0] - this.viewport[0][0]);
        this.scaleY = this.getSize().getHeight() / (this.viewport[0][1] - this.viewport[1][1]);
        if (this.graphicsData != null && this.graphicsData.size() != 0) {// Есть ли данные по графику
            Graphics2D canvas = (Graphics2D)g;
            this.paintGrid(canvas);
            this.paintAxis(canvas);
            this.paintGraphics(canvas);
            this.paintMarkers(canvas);
            this.paintLabels(canvas);
            this.paintSelection(canvas);
        }
    }//Нарисует График оси сетку маркеры и др.

    private void paintSelection(Graphics2D canvas) {
        if (this.scaleMode) {
            canvas.setStroke(this.selectionStroke);
            canvas.setColor(Color.BLACK);
            canvas.draw(this.selectionRect);
        }
    }// Что он рисует связанное с мышью???????????? Предположительно штрихованный прямоугольник как новый фрейм

    private void paintGraphics(Graphics2D canvas) {
        canvas.setStroke(this.markerStroke);
        canvas.setColor(Color.RED);
        Double currentX = null;
        Double currentY = null;
        Iterator var5 = this.graphicsData.iterator();

        if(!changeView){
            while (var5.hasNext()) {
                Double[] point = (Double[]) var5.next();
                if (point[0] >= this.viewport[0][0] && point[1] <= this.viewport[0][1] && point[0] <= this.viewport[1][0] && point[1] >= this.viewport[1][1]) {
                    if (currentX != null && currentY != null) {
                        canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(currentX, currentY), this.translateXYtoPoint(point[0], point[1])));
                    }

                    currentX = point[0];
                    currentY = point[1];
                }
            }
        }
        else {
            Dx += dx;
            Dy += dy;
            while (var5.hasNext()) {
                Double[] point = (Double[]) var5.next();
                if (point[0] >= this.viewport[0][0] && point[1] <= this.viewport[0][1] && point[0] <= this.viewport[1][0] && point[1] >= this.viewport[1][1]) {
                    if (currentX != null && currentY != null) {
                        canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(currentX, currentY), this.translateXYtoPoint(point[0] + Dx, point[1] + Dy)));
                    }

                    currentX = point[0] + Dx;
                    currentY = point[1] + Dy;
                }
            }
        }
    }//

    private void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(this.markerStroke);
        canvas.setColor(Color.RED);
        canvas.setPaint(Color.RED);
        java.awt.geom.Ellipse2D.Double lastMarker = null;
        int i = -1;
        Iterator var5 = this.graphicsData.iterator();

        if(!changeView){
            while(var5.hasNext()) {
                Double[] point = (Double[])var5.next();
                ++i;
                if (point[0] >= this.viewport[0][0] && point[1] <= this.viewport[0][1] && point[0] <= this.viewport[1][0] && point[1] >= this.viewport[1][1]) {
                    byte radius;
                    if (i == this.selectedMarker) {
                        radius = 6;
                    } else {
                        radius = 3;
                    }

                    java.awt.geom.Ellipse2D.Double marker = new java.awt.geom.Ellipse2D.Double();
                    Point2D center = this.translateXYtoPoint(point[0], point[1]);
                    Point2D corner = new java.awt.geom.Point2D.Double(center.getX() + (double)radius, center.getY() + (double)radius);
                    marker.setFrameFromCenter(center, corner);
                    if (i == this.selectedMarker) {
                        lastMarker = marker;
                    } else {
                        canvas.draw(marker);
                        canvas.fill(marker);
                    }
                }
            }
        }
        else {
            Dx += dx;
            Dy += dy;
            while(var5.hasNext()) {
                Double[] point = (Double[])var5.next();
                ++i;
                if (point[0] >= this.viewport[0][0] && point[1] <= this.viewport[0][1] && point[0] <= this.viewport[1][0] && point[1] >= this.viewport[1][1]) {
                    byte radius;
                    if (i == this.selectedMarker) {
                        radius = 6;
                    } else {
                        radius = 3;
                    }

                    java.awt.geom.Ellipse2D.Double marker = new java.awt.geom.Ellipse2D.Double();
                    Point2D center = this.translateXYtoPoint(point[0] + Dx, point[1] + Dy);
                    Point2D corner = new java.awt.geom.Point2D.Double(center.getX() + (double)radius, center.getY() + (double)radius);
                    marker.setFrameFromCenter(center, corner);
                    if (i == this.selectedMarker) {
                        lastMarker = marker;
                    } else {
                        canvas.draw(marker);
                        canvas.fill(marker);
                    }
                }
            }
        }


        if (lastMarker != null) {
            canvas.setColor(Color.BLUE);
            canvas.setPaint(Color.BLUE);
            canvas.draw(lastMarker);
            canvas.fill(lastMarker);
        }

    }//

    private void paintLabels(Graphics2D canvas) {
        canvas.setColor(Color.BLACK);
        canvas.setFont(this.labelsFont);
        FontRenderContext context = canvas.getFontRenderContext();
        double labelYPos;
        if (this.viewport[1][1] < 0.0D && this.viewport[0][1] > 0.0D) {
            labelYPos = 0.0D;
        } else {
            labelYPos = this.viewport[1][1];
        }

        double labelXPos;
        if (this.viewport[0][0] < 0.0D && this.viewport[1][0] > 0.0D) {
            labelXPos = 0.0D;
        } else {
            labelXPos = this.viewport[0][0];
        }

        double pos = this.viewport[0][0];

        double step;
        java.awt.geom.Point2D.Double point;
        String label;
        Rectangle2D bounds;

        if(!changeView){
            for(step = (this.viewport[1][0] - this.viewport[0][0]) / 10.0D; pos < this.viewport[1][0]; pos += step) {
                point = this.translateXYtoPoint(pos, labelYPos);
                label = formatter.format(pos);
                bounds = this.labelsFont.getStringBounds(label, context);
                canvas.drawString(label, (float)(point.getX() + 5.0D), (float)(point.getY() - bounds.getHeight()));
            }

            pos = this.viewport[1][1];

            for(step = (this.viewport[0][1] - this.viewport[1][1]) / 10.0D; pos < this.viewport[0][1]; pos += step) {
                point = this.translateXYtoPoint(labelXPos, pos);
                label = formatter.format(pos);
                bounds = this.labelsFont.getStringBounds(label, context);
                canvas.drawString(label, (float)(point.getX() + 5.0D), (float)(point.getY() - bounds.getHeight()));
            }

            if (this.selectedMarker >= 0) {
                point = this.translateXYtoPoint(((Double[])this.graphicsData.get(this.selectedMarker))[0], ((Double[])this.graphicsData.get(this.selectedMarker))[1]);
                label = "X=" + formatter.format(((Double[])this.graphicsData.get(this.selectedMarker))[0]) + ", Y=" + formatter.format(((Double[])this.graphicsData.get(this.selectedMarker))[1]);
                bounds = this.labelsFont.getStringBounds(label, context);
                canvas.setColor(Color.BLUE);
                canvas.drawString(label, (float)(point.getX() + 5.0D), (float)(point.getY() - bounds.getHeight()));
            }
        }
        else{
            Dx += dx;
            Dy += dy;
            for(step = (this.viewport[1][0] - this.viewport[0][0]) / 10.0D; pos < this.viewport[1][0]; pos += step) {
                point = this.translateXYtoPoint(pos + Dx, labelYPos);
                label = formatter.format(pos);
                bounds = this.labelsFont.getStringBounds(label, context);
                canvas.drawString(label, (float)(point.getX() + 5.0D), (float)(point.getY() - bounds.getHeight()));
            }

            pos = this.viewport[1][1];

            for(step = (this.viewport[0][1] - this.viewport[1][1]) / 10.0D; pos < this.viewport[0][1]; pos += step) {
                point = this.translateXYtoPoint(labelXPos, pos + Dy);
                label = formatter.format(pos);
                bounds = this.labelsFont.getStringBounds(label, context);
                canvas.drawString(label, (float)(point.getX() + 5.0D), (float)(point.getY() - bounds.getHeight()));
            }

            if (this.selectedMarker >= 0) {
                point = this.translateXYtoPoint(((Double[])this.graphicsData.get(this.selectedMarker))[0], ((Double[])this.graphicsData.get(this.selectedMarker))[1]);
                label = "X=" + formatter.format(((Double[])this.graphicsData.get(this.selectedMarker))[0]) + ", Y=" + formatter.format(((Double[])this.graphicsData.get(this.selectedMarker))[1]);
                bounds = this.labelsFont.getStringBounds(label, context);
                canvas.setColor(Color.BLUE);
                canvas.drawString(label, (float)(point.getX() + 5.0D), (float)(point.getY() - bounds.getHeight()));
            }
        }
    }//Подписывает "деления" на сетке ------- Проверить SelectedMarker в этом методе ----------

    private void paintGrid(Graphics2D canvas) {
        canvas.setStroke(this.gridStroke);
        canvas.setColor(Color.GRAY);
        double pos = this.viewport[0][0];

        double step;
        if (!changeView) {
            for (step = (this.viewport[1][0] - this.viewport[0][0]) / 10.0D; pos < this.viewport[1][0]; pos += step) {
                canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(pos, this.viewport[0][1]), this.translateXYtoPoint(pos, this.viewport[1][1])));// Рисует вертикальные прямые
            }

            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[1][0], this.viewport[0][1]), this.translateXYtoPoint(this.viewport[1][0], this.viewport[1][1])));//Самая крйняя правая вертикальная прямая
            pos = this.viewport[1][1];

            for (step = (this.viewport[0][1] - this.viewport[1][1]) / 10.0D; pos < this.viewport[0][1]; pos += step) {
                canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[0][0], pos), this.translateXYtoPoint(this.viewport[1][0], pos)));//Рисует горизонтальные линии
            }

            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[0][0], this.viewport[0][1]), this.translateXYtoPoint(this.viewport[1][0], this.viewport[0][1])));//Самая верхняя горизонтальная линия
        }
        else {
            Dx += dx;
            Dy += dy;
            dx = 0;
            dy = 0;
            for (step = (this.viewport[1][0] - this.viewport[0][0]) / 10.0D; pos < this.viewport[1][0]; pos += step) {
                canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(pos + Dx, this.viewport[0][1]), this.translateXYtoPoint(pos + Dx, this.viewport[1][1])));// Рисует вертикальные прямые
            }

            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[1][0], this.viewport[0][1]), this.translateXYtoPoint(this.viewport[1][0], this.viewport[1][1])));//Самая крйняя правая вертикальная прямая
            pos = this.viewport[1][1];

            for (step = (this.viewport[0][1] - this.viewport[1][1]) / 10.0D; pos < this.viewport[0][1]; pos += step) {
                canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[0][0], pos + Dy), this.translateXYtoPoint(this.viewport[1][0], pos + Dy)));//Рисует горизонтальные линии
            }

            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[0][0], this.viewport[0][1]), this.translateXYtoPoint(this.viewport[1][0], this.viewport[0][1])));//Самая верхняя горизонтальная линия
        }
    }//Рисует сетку

    private void paintAxis(Graphics2D canvas) {
        canvas.setStroke(this.axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setFont(this.axisFont);
        FontRenderContext context = canvas.getFontRenderContext();
        Rectangle2D bounds;
        java.awt.geom.Point2D.Double labelPos;

        if (!changeView && this.viewport[0][0] <= 0.0D && this.viewport[1][0] >= 0.0D && dx == 0) {
            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(0.0D, this.viewport[0][1]), this.translateXYtoPoint(dx, this.viewport[1][1]))); //рисуется ось Oy от (0,maxY) до (0,minY)

            GeneralPath arrow = new GeneralPath();      // Стрелка оси Y
            Point2D.Double lineEnd = translateXYtoPoint(0.0D, viewport[0][1]);// Установить начальную точку ломаной точно на верхний конец оси Y
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            // Вести левый "скат" стрелки в точку с относительными координатами (5,20)
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 20);
            // Вести нижнюю часть стрелки в точку с относительными координатами (-10, 0)
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
            arrow.closePath();  // Замкнуть треугольник стрелки
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку

            bounds = this.axisFont.getStringBounds("y", context);
            labelPos = this.translateXYtoPoint(0.0D, this.viewport[0][1]);
            canvas.drawString("y", (float)labelPos.x + 10.0F, (float)(labelPos.y + bounds.getHeight() / 2.0D));// Послдение три строки рисуют нпдпись для оси Oy
        }//dx == 0

        if (!changeView && this.viewport[1][1] <= 0.0D && this.viewport[0][1] >= 0.0D && dy == 0) {
            canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[0][0], 0.0D), this.translateXYtoPoint(this.viewport[1][0], 0.0D)));//рисуется ось Ox

            GeneralPath arrow = new GeneralPath();  // Стрелка оси X
            Point2D.Double lineEnd = translateXYtoPoint(viewport[1][0], 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20, arrow.getCurrentPoint().getY() - 5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);
            arrow.closePath();  // Замкнуть треугольник стрелки
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку

            bounds = this.axisFont.getStringBounds("x", context);
            labelPos = this.translateXYtoPoint(this.viewport[1][0], 0.0D);
            canvas.drawString("x", (float)(labelPos.x - bounds.getWidth() - 10.0D), (float)(labelPos.y - bounds.getHeight() / 2.0D));// Послдение три строки рисуют нпдпись для оси Ox
            a++;
        }//dy == 0

        if (a != 0 && this.viewport[0][0] <= 0.0D && this.viewport[1][0] >= 0.0D) {
            if (changeView) {
                Dx += dx;
                //dx = 0 ;
                canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(Dx, this.viewport[0][1]), this.translateXYtoPoint(Dx, this.viewport[1][1]))); //рисуется ось Oy от (0,maxY) до (0,minY)
            GeneralPath arrow = new GeneralPath();      // Стрелка оси Y
            Point2D.Double lineEnd = translateXYtoPoint(Dx, viewport[0][1]);// Установить начальную точку ломаной точно на верхний конец оси Y
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 20);
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
            arrow.closePath();  // Замкнуть треугольник стрелки
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку

            bounds = this.axisFont.getStringBounds("y", context);
            labelPos = this.translateXYtoPoint(Dx, this.viewport[0][1]);
            canvas.drawString("y", (float)labelPos.x + 10.0F, (float)(labelPos.y + bounds.getHeight() / 2.0D));// Послдение три строки рисуют нпдпись для оси Oy
            }
        }// delete dx = 0;

        if (a != 0 && this.viewport[1][1] <= 0.0D && this.viewport[0][1] >= 0.0D) {
            if (changeView){
                Dy += dy;
                //dy = 0;
                canvas.draw(new java.awt.geom.Line2D.Double(this.translateXYtoPoint(this.viewport[0][0], Dy), this.translateXYtoPoint(this.viewport[1][0], Dy)));//рисуется ось Ox
            GeneralPath arrow = new GeneralPath();  // Стрелка оси X
            Point2D.Double lineEnd = translateXYtoPoint(viewport[1][0], Dy);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20, arrow.getCurrentPoint().getY() - 5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);
            arrow.closePath();  // Замкнуть треугольник стрелки
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку

            bounds = this.axisFont.getStringBounds("x", context);
            labelPos = this.translateXYtoPoint(this.viewport[1][0], Dy);
            canvas.drawString("x", (float)(labelPos.x - bounds.getWidth() - 10.0D), (float)(labelPos.y - bounds.getHeight() / 2.0D));// Послдение три строки рисуют нпдпись для оси Ox
            }
        }// delete dy = 0;


    }//Рисует Оси и их стрелки

    protected java.awt.geom.Point2D.Double translateXYtoPoint(double x, double y) {
        double deltaX = x - this.viewport[0][0];
        double deltaY = this.viewport[0][1] - y;
        return new java.awt.geom.Point2D.Double(deltaX * this.scaleX, deltaY * this.scaleY);
    }//

    protected double[] translatePointToXY(int x, int y) {
        return new double[]{this.viewport[0][0] + (double)x / this.scaleX, this.viewport[0][1] - (double)y / this.scaleY};
    }//

    protected int findSelectedPoint(int x, int y) {
        if (this.graphicsData == null) {
            return -1;
        } else {
            int pos = 0;

            for(Iterator var5 = this.graphicsData.iterator(); var5.hasNext(); ++pos) {
                Double[] point = (Double[])var5.next();
                java.awt.geom.Point2D.Double screenPoint = this.translateXYtoPoint(point[0], point[1]);
                double distance = (screenPoint.getX() - (double)x) * (screenPoint.getX() - (double)x) + (screenPoint.getY() - (double)y) * (screenPoint.getY() - (double)y);// Вроде квадрат гипотенузы
                if (distance < 100.0D) {
                    return pos;//Для чего pos?????????
                }
            }

            return -1;
        }
    }//

    public void reset() {
        this.displayGraphics(this.originalData);
    }//

    public class MouseHandler extends MouseAdapter {
        public MouseHandler() {
        }

        public void mouseClicked(MouseEvent ev) {
            if (ev.getButton() == 3) {
                if (GraphicsDisplay.this.undoHistory.size() > 0) {
                    GraphicsDisplay.this.viewport = (double[][])GraphicsDisplay.this.undoHistory.get(GraphicsDisplay.this.undoHistory.size() - 1);
                    GraphicsDisplay.this.undoHistory.remove(GraphicsDisplay.this.undoHistory.size() - 1);
                } else {
                    GraphicsDisplay.this.zoomToRegion(GraphicsDisplay.this.minX, GraphicsDisplay.this.maxY, GraphicsDisplay.this.maxX, GraphicsDisplay.this.minY);
                }

                GraphicsDisplay.this.repaint();
            }

        }

        public void mousePressed(MouseEvent ev) {
            if (ev.getButton() == 1) {
                GraphicsDisplay.this.selectedMarker = GraphicsDisplay.this.findSelectedPoint(ev.getX(), ev.getY());
                GraphicsDisplay.this.originalPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());// Начальная точка штрихованного прямоугольника
                if (GraphicsDisplay.this.selectedMarker >= 0) {
                    GraphicsDisplay.this.changeMode = true;
                    GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(8));// устанавливает тип курсора РАССШИРЕНИЯ
                } else {
                    GraphicsDisplay.this.scaleMode = true;
                    GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(5));// устанавливает тип курсора
                    GraphicsDisplay.this.selectionRect.setFrame((double)ev.getX(), (double)ev.getY(), 1.0D, 1.0D);//Для рисовки штрихованного прямоугольника
                }

            }

            if(ev.getButton() == 2){

                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(1));// устанавливает тип курсора ПЕРЕМЕЩЕНИЕ
                GraphicsDisplay.this.originalPoint_2 = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());// Начальная точка
                System.out.println("x = "+ originalPoint_2[0] + " y = " +   originalPoint_2[1] + " Start Point");

            }

        }

        public void mouseReleased(MouseEvent ev) {
            if (ev.getButton() == 1) {
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(0));
                if (GraphicsDisplay.this.changeMode) {
                    GraphicsDisplay.this.changeMode = false;
                } else {
                    GraphicsDisplay.this.scaleMode = false;
                    double[] finalPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());// Последняя координата в штрифовоном прямоугольнике
                    GraphicsDisplay.this.undoHistory.add(GraphicsDisplay.this.viewport);//Сохраняет диапозон значений графика перед "приближением"
                    GraphicsDisplay.this.viewport = new double[2][2];//Создаёт новый диапазон
                    GraphicsDisplay.this.zoomToRegion(GraphicsDisplay.this.originalPoint[0], GraphicsDisplay.this.originalPoint[1], finalPoint[0], finalPoint[1]);//Рисует "Приближение" по сути новый Фрейм
                    GraphicsDisplay.this.repaint();//Зачем тут второй repaint если он вызовется в zoomToRegion
                }

            }

            if(ev.getButton() == 2){
                double[] finalPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());// Последняя координата
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(0));

                dx = finalPoint[0] - originalPoint_2[0];
                dy = finalPoint[1] - originalPoint_2[1];
                System.out.println("x = " + finalPoint[0] + " y = " + finalPoint[1] + " End Point");
                System.out.println("dx = "+ dx + " dy = " + dy);

                double VectorSmechenia = Math.sqrt ((dx * dx) + (dy * dy));
                System.out.println("VectorSmechenia = " + VectorSmechenia);
                System.out.println(" ");

                if (VectorSmechenia > 0){
                    changeView = true;//-------------------------------------------
                }
                GraphicsDisplay.this.repaint();
            }

        }
    }//Для отслеживания нажатий кнопок мыши    ----------- ПРИДУМАЙ УСЛОВИЕ ВЫКЛЮЧЕНИЯ changeView ------------------------

    public class MouseMotionHandler implements MouseMotionListener {
        public MouseMotionHandler() {
        }

        public void mouseMoved(MouseEvent ev) {
            GraphicsDisplay.this.selectedMarker = GraphicsDisplay.this.findSelectedPoint(ev.getX(), ev.getY());
            if (GraphicsDisplay.this.selectedMarker >= 0) {
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(8));
            } else {
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(0));
            }

            GraphicsDisplay.this.repaint();
        }

        public void mouseDragged(MouseEvent ev) {
            if (GraphicsDisplay.this.changeMode) {
                double[] currentPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());
                double newY = ((Double[])GraphicsDisplay.this.graphicsData.get(GraphicsDisplay.this.selectedMarker))[1] + (currentPoint[1] - ((Double[])GraphicsDisplay.this.graphicsData.get(GraphicsDisplay.this.selectedMarker))[1]);
                if (newY > GraphicsDisplay.this.viewport[0][1]) {
                    newY = GraphicsDisplay.this.viewport[0][1];
                }

                if (newY < GraphicsDisplay.this.viewport[1][1]) {
                    newY = GraphicsDisplay.this.viewport[1][1];
                }

                ((Double[])GraphicsDisplay.this.graphicsData.get(GraphicsDisplay.this.selectedMarker))[1] = newY;
                GraphicsDisplay.this.repaint();
            } else {
                double width = (double)ev.getX() - GraphicsDisplay.this.selectionRect.getX();
                if (width < 5.0D) {
                    width = 5.0D;
                }

                double height = (double)ev.getY() - GraphicsDisplay.this.selectionRect.getY();
                if (height < 5.0D) {
                    height = 5.0D;
                }

                GraphicsDisplay.this.selectionRect.setFrame(GraphicsDisplay.this.selectionRect.getX(), GraphicsDisplay.this.selectionRect.getY(), width, height);
                GraphicsDisplay.this.repaint();
            }

        }
    }//Для отслеживания положения мыши на экране
}
