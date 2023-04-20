package org.example;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import processing.core.PApplet;

/**
 * LEE Y ESCRIBE EN ORACLE DESDE POSTGRES
 */
public class ConnPG extends PApplet {
    PGConnection connPG = PGConnection.getInstance();
    ArrayList<Target> targets = new ArrayList<Target>();
    String x = "0";
    String y = "0";
    int score = 0;
    int timer = 0;
    int spawnInterval = 100;
    boolean cursorInsideWindow=false;
    public ConnPG() throws SQLException {

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
            readOracle();
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
                    scorePG();
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
    void scorePG(){
        try {
            Statement stmt = connPG.getConnection().createStatement();
            stmt.executeUpdate("UPDATE link.score_oracle SET score=" + score + " WHERE id=1");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    void drawCrosshair() {
        float size = 20;
        stroke(255, 0, 0);
        line(Float.parseFloat(x) - size / 2, Float.parseFloat(y), Float.parseFloat(x) + size / 2, Float.parseFloat(y));
        line(Float.parseFloat(x), Float.parseFloat(y) - size / 2, Float.parseFloat(x), Float.parseFloat(y) + size / 2);
    }
    @Override
    public void mouseMoved() {
        cursorInsideWindow = true;
    }
    @Override
    public void mouseExited() {
        cursorInsideWindow = false;
    }

    private void readScore() {
        try {
            Statement stmt = connPG.getConnection().createStatement();
            stmt.executeQuery("SELECT * FROM score_pg score WHERE score.id=1");
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                int score = rs.getInt("score");
                textSize(20);
                text("Score: "+score, (float) width / 2 - 30, height - 30);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void readOracle() throws SQLException {
        Statement stmt = connPG.getConnection().createStatement();
        stmt.executeQuery("SELECT * FROM link.mouse_pos_oracle mouse WHERE mouse.id=1");
        ResultSet rs = stmt.getResultSet();
        while (rs.next()) {
            x = rs.getString("x");
            y = rs.getString("y");
            textSize(20);
            text(x + " " + y, (float) width / 2 - 15, 30);
        }
        stmt.executeUpdate("UPDATE mouse_pos_pg mouse SET x=" + mouseX + ", y=" + mouseY + " WHERE id=1");
        stmt.close();
    }


    public static void main(String[] args) {
        try {
            ConnPG connPG = new ConnPG();
            PApplet.runSketch(new String[]{"ConnPG"}, connPG);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void dispose(){
        super.dispose();
        try {
            connPG.getConnection().close();
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
