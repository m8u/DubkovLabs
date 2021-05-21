package dev.m8u.dubkovlabsserver;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Henhouse {

    int canvasWidth = 485, canvasHeight = 559;

    Instant startTime;

    Timer timer;
    long currentSec;
    Instant pauseTime;

    boolean isPaused = false;

    int frameCount;

    Random random;
    float currentRandomFloat;

    int N1 = 3, N2 = 1, birdsLifespan = 30;
    final int birdsCountLimit = 15;
    float K = 0.5f, P = 0.5f;

    long mamaLastSec = -1, childLastSec = -1;
    long randomFloatLastSec = -1;

    ArrayList<MamaBird> mamaBirds;
    ArrayList<ChildBird> childBirds;
    ArrayList<MamaBird> deadBirds;

    Henhouse() {
        startTime = Instant.now();
        random = new Random();
        mamaBirds = new ArrayList<>();
        childBirds = new ArrayList<>();
        deadBirds = new ArrayList<>();
        frameCount = 0;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                tick();
            }
        }, 0, 16);
    }

    void tick() {
        currentSec = Duration.between(startTime, Instant.now()).getSeconds();

        if (currentSec != randomFloatLastSec ) {
            currentRandomFloat = random.nextFloat();
            randomFloatLastSec = currentSec;
        }
        if (currentSec != mamaLastSec) {
            for (MamaBird bird : mamaBirds) {
                bird.secondsAlive++;
            }
            if (currentSec % N1 == 0 && currentRandomFloat <= P && countBirdsOverall() <= birdsCountLimit) {
                mamaBirds.add(new MamaBird(170 + random.nextInt(canvasWidth - 170*2),
                        170 + random.nextInt(canvasHeight - 170*2),
                        1 + random.nextInt(2),
                        1 + random.nextInt(2),
                        0,
                        1 + random.nextInt(3),
                        birdsLifespan,
                        (int) (birdsLifespan / 3.0f)));
            }
            mamaLastSec = currentSec;
        }
        if (currentSec != childLastSec) {
            for (ChildBird bird : childBirds) {
                bird.secondsAlive++;
            }
            if (currentSec % N2 == 0 && (float) childBirds.size() / mamaBirds.size() < K && countBirdsOverall() <= birdsCountLimit) {
                childBirds.add(new ChildBird((canvasWidth/2 + (random.nextInt(50) - 25)),
                        (canvasHeight/2 + (random.nextInt(50) - 25)),
                        2 + random.nextInt(5),
                        2 + random.nextInt(5),
                        0,
                        6 + random.nextInt(10),
                        birdsLifespan));
                childLastSec = currentSec;
            }
            childLastSec = currentSec;
        }

        for (Iterator<MamaBird> iterator = mamaBirds.iterator(); iterator.hasNext();) {
            MamaBird bird = iterator.next();
            if (bird.secondsAlive > bird.lifespan) {
                iterator.remove();
                deadBirds.add(new MamaBird(bird.x, bird.y, 0, -4, 0, 0,
                        bird.lifespan, bird.lifespan + 1));
            }
            bird.animationStep();
            if (bird.checkForBorders(canvasWidth, canvasHeight) && bird.collisionsInRow < 50) {
                bird.shouldCluck = true;
            }
        }
        for (Iterator<ChildBird> iterator = childBirds.iterator(); iterator.hasNext();) {
            ChildBird bird = iterator.next();
            if (bird.secondsAlive >= (int) (bird.lifespan / 3.0f)) {
                iterator.remove();
                mamaBirds.add(new MamaBird(bird.x, bird.y,
                        (bird.xVel > 0 ? 1 + random.nextInt(2) : (1 + random.nextInt(2)) * -1),
                        (bird.yVel > 0 ? 1 + random.nextInt(2) : 1 + (random.nextInt(2)) * -1),
                        bird.angle,
                        (bird.angleVel > 0 ? 1 + random.nextInt(3) : (1 + random.nextInt(3)) * -1),
                        bird.lifespan,
                        (int) (bird.lifespan / 3.0f)));
            }
            if (bird.secondsAlive >= bird.lifespan / 6.0f)
                bird.animationStep();
            if (bird.checkForBorders(canvasWidth, canvasHeight) && bird.collisionsInRow < 50) {
                bird.shouldCluck = true;
            }
        }

        for (Iterator<MamaBird> iterator = deadBirds.iterator(); iterator.hasNext();) {
            MamaBird bird = iterator.next();
            if (bird.y + bird.getImageWidth() < 0) {
                iterator.remove();
            }
            bird.animationStep();
        }

        frameCount++;
    }

    public void togglePause(boolean handleStartTimeValue) {
        if (!isPaused) {
            timer.cancel();
            if (handleStartTimeValue)
                pauseTime = Instant.now();
            isPaused = true;
        } else {
            if (handleStartTimeValue)
                startTime = startTime.plus(Duration.between(pauseTime, Instant.now()));
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    tick();
                }
            }, 0, 16);
            isPaused = false;
        }
    }

    public void saveToDatabase() {
        Connection conn;
        Statement stmt;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/dubkovlabs?" +
                            "user=m8u&password=3369");
            stmt = conn.createStatement();

            stmt.executeQuery("DELETE FROM parameters WHERE true");
            stmt.executeQuery(
                String.format("INSERT INTO parameters (N1, P, N2, K, birdsLifespan, currentSec) " +
                                "VALUES (%d, %f, %d, %f, %d, %d)",
                    N1, P, N2, K, birdsLifespan, currentSec));
            stmt.executeQuery("DELETE FROM birds WHERE true");
            for (MamaBird bird : mamaBirds) {
                stmt.executeQuery(
                    String.format("INSERT INTO birds (isAdult, x, y, xVel, yVel, angle, angleVel, lifespan, secondsAlive) " +
                                    "VALUES (1, %d, %d, %d, %d, %d, %d, %d, %d)",
                        bird.x, bird.y, bird.xVel, bird.yVel, bird.angle, bird.angleVel, bird.lifespan, bird.secondsAlive));
            }
            for (ChildBird bird : childBirds) {
                stmt.executeQuery(
                    String.format("INSERT INTO birds (isAdult, x, y, xVel, yVel, angle, angleVel, lifespan, secondsAlive) " +
                                    "VALUES (0, %d, %d, %d, %d, %d, %d, %d, %d)",
                        bird.x, bird.y, bird.xVel, bird.yVel, bird.angle, bird.angleVel, bird.lifespan, bird.secondsAlive));
            }

        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found");
        }
    }

    public void loadFromDatabase() {
        Connection conn;
        Statement stmt;
        ResultSet rs;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/dubkovlabs?" +
                    "user=m8u&password=3369");
            stmt = conn.createStatement();

            rs = stmt.executeQuery("SELECT * FROM parameters");
            rs.first();
            N1 = rs.getInt("N1");
            P = rs.getFloat("P");
            N2 = rs.getInt("N2");
            K = rs.getFloat("K");
            birdsLifespan = rs.getInt("birdsLifespan");
            currentSec = rs.getLong("currentSec");

            rs = stmt.executeQuery("SELECT * FROM birds");
            rs.first();
            mamaBirds = new ArrayList<>();
            childBirds = new ArrayList<>();
            deadBirds = new ArrayList<>();
            while (rs.next()) {
                if (rs.getBoolean("isAdult")) {
                    mamaBirds.add(new MamaBird(
                            rs.getInt("x"),
                            rs.getInt("y"),
                            rs.getInt("xVel"),
                            rs.getInt("yVel"),
                            rs.getInt("angle"),
                            rs.getInt("angleVel"),
                            rs.getInt("lifespan"),
                            rs.getInt("secondsAlive")
                    ));
                } else {
                    childBirds.add(new ChildBird(
                            rs.getInt("x"),
                            rs.getInt("y"),
                            rs.getInt("xVel"),
                            rs.getInt("yVel"),
                            rs.getInt("angle"),
                            rs.getInt("angleVel"),
                            rs.getInt("lifespan"),
                            rs.getInt("secondsAlive")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found");
        }
    }

    int countBirdsOverall() {
        return mamaBirds.size() + childBirds.size() + deadBirds.size();
    }

    JSONObject getJSON() {
        JSONObject rootObject = new JSONObject();
        rootObject.put("currentSec", currentSec);
        rootObject.put("N1", N1);
        rootObject.put("N2", N2);
        rootObject.put("K", K);
        rootObject.put("P", P);
        rootObject.put("birdsLifespan", birdsLifespan);
        rootObject.put("isPaused", isPaused);
        JSONArray mamaBirdsArray = new JSONArray();
        for (MamaBird bird : mamaBirds) {
            JSONObject jsonMamaBird = new JSONObject();
            jsonMamaBird.put("x", bird.x);
            jsonMamaBird.put("y", bird.y);
            jsonMamaBird.put("angle", bird.angle);
            jsonMamaBird.put("lifespan", bird.lifespan);
            jsonMamaBird.put("secondsAlive", bird.secondsAlive);
            jsonMamaBird.put("isStuck", bird.isStuck);
            jsonMamaBird.put("shouldCluck", bird.shouldCluck);
            bird.shouldCluck = false;
            mamaBirdsArray.put(jsonMamaBird);
        }
        JSONArray childBirdsArray = new JSONArray();
        for (ChildBird bird : childBirds) {
            JSONObject jsonChildBird = new JSONObject();
            jsonChildBird.put("x", bird.x);
            jsonChildBird.put("y", bird.y);
            jsonChildBird.put("angle", bird.angle);
            jsonChildBird.put("lifespan", bird.lifespan);
            jsonChildBird.put("secondsAlive", bird.secondsAlive);
            jsonChildBird.put("isStuck", bird.isStuck);
            jsonChildBird.put("shouldCluck", bird.shouldCluck);
            bird.shouldCluck = false;
            childBirdsArray.put(jsonChildBird);
        }
        JSONArray deadBirdsArray = new JSONArray();
        for (MamaBird bird : deadBirds) {
            JSONObject jsonDeadBird = new JSONObject();
            jsonDeadBird.put("x", bird.x);
            jsonDeadBird.put("y", bird.y);
            jsonDeadBird.put("angle", bird.angle);
            jsonDeadBird.put("lifespan", bird.lifespan);
            jsonDeadBird.put("secondsAlive", bird.secondsAlive);
            jsonDeadBird.put("isStuck", bird.isStuck);
            jsonDeadBird.put("shouldCluck", bird.shouldCluck);
            bird.shouldCluck = false;
            deadBirdsArray.put(jsonDeadBird);
        }
        rootObject.put("mamaBirds", mamaBirdsArray);
        rootObject.put("childBirds", childBirdsArray);
        rootObject.put("deadBirds", deadBirdsArray);

        return rootObject;
    }
}
