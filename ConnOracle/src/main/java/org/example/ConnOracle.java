package org.example;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import processing.core.PApplet;

/**
 * LEE DESDE Y ESCRIBE EN POSTGRES DESDE ORACLE
 */
public class ConnOracle extends PApplet {
    OracleConnection connOracle = OracleConnection.getInstance();
    ArrayList<Target> targets = new ArrayList<Target>();
    String x = "0";
    String y = "0";
    int score = 0;
    int timer = 0;
    int spawnInterval = 100;
    boolean cursorInsideWindow=false;
    public ConnOracle() throws SQLException {

    }

    @Override
    public void settings() {
        size(500, 500);
    }

    @Override
    public void setup() {
        background(0);
    }

    @Override
    public void draw() {
        background(0);
        ArrayList<Target> newTargets = new ArrayList<Target>();
        timer++;
        try {
            readPG();
            readScore();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if(!cursorInsideWindow){
            if (timer % spawnInterval == 0) {
                float x = random(width);
                float y = random(height);
                float size = random(20, 50);
                targets.add(new Target(x, y, size));
            }
            for (Target t : targets) {
                t.display();
                if(!t.hit && dist(Float.parseFloat(x), Float.parseFloat(y), t.x, t.y) <= t.size/2){
                    t.hit = true;
                    score += 5;
                    scoreOracle();
                    float x = random(width);
                    float y = random(height);
                    float size = random(20, 50);
                    newTargets.add(new Target(x, y, size));
                }
            }
            targets.addAll(newTargets);
            drawCrosshair();
        }
    }
    @Override
    public void mouseMoved() {
        cursorInsideWindow = true;
    }
    @Override
    public void mouseExited() {
        cursorInsideWindow = false;
    }

    public void scoreOracle(){
        try {
            Statement stmt = connOracle.getConnection().createStatement();
            stmt.executeUpdate("UPDATE \"score_pg\"@pg score SET score.\"score\"=" + score + " WHERE score.\"id\"=1");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void readScore(){
        try{
            Statement stmt = connOracle.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM score_oracle WHERE id=1");
            while (rs.next()) {
                int score = rs.getInt("score");
                textSize(20);
                text("Score: " + score, (float) width / 2 - 30, height - 30);
                fill(255);
            }
            stmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    void drawCrosshair() {
        float size = 20;
        stroke(255, 0, 0); 
        line(Float.parseFloat(x) - size / 2, Float.parseFloat(y), Float.parseFloat(x) + size / 2, Float.parseFloat(y));
        line(Float.parseFloat(x), Float.parseFloat(y) - size / 2, Float.parseFloat(x), Float.parseFloat(y) + size / 2);
    }
    private void readPG() throws SQLException {

        Statement stmt = connOracle.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM \"mouse_pos_pg\"@pg mouse WHERE mouse.\"id\"=1");
        while (rs.next()) {
            x = rs.getString("x");
            y = rs.getString("y");
            textSize(20);
            text(x + " " + y, (float) width / 2 - 15, 30);
                   
        }
        stmt.executeUpdate("UPDATE mouse_pos_oracle mouse SET x=" + mouseX + ", y=" + mouseY + " WHERE id=1");
        stmt.close();

    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            connOracle.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        try {
            ConnOracle connOracle = new ConnOracle();
            PApplet.runSketch(new String[]{"ConnOracle"}, connOracle);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    class Target {
        float x, y, size;
        boolean hit;

        Target(float x, float y, float size) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.hit=false;
        }

        void display() {
            if(!hit){
                stroke(255, 255, 255);
                fill(255); // Color blanco
                ellipse(x, y, size, size);
            }
        }

    }
}
