package dev.m8u.dubkovlabsserver;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Paths;
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

    public void save() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(Paths.get(".")+"/save.sav"));
            out.writeObject(N1);
            out.writeObject(P);
            out.writeObject(N2);
            out.writeObject(K);
            out.writeObject(birdsLifespan);
            out.writeObject(currentSec);
            for (MamaBird bird : mamaBirds) {
                out.writeObject(bird);
            }
            for (ChildBird bird : childBirds) {
                out.writeObject(bird);
            }
            out.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void load() {
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(Paths.get(".")+"/save.sav"));
            N1 = (int) in.readObject();
            P = (float) in.readObject();
            N2 = (int) in.readObject();
            K = (float) in.readObject();
            birdsLifespan = (int) in.readObject();
            startTime = Instant.now().minusSeconds((Long) in.readObject());
            Object object;
            while (true) {
                object = in.readObject();
                if (object.getClass().equals(MamaBird.class)) {
                    mamaBirds.add((MamaBird) object);
                } else {
                    childBirds.add((ChildBird) object);
                }
            }
        } catch (ClassNotFoundException | IOException exception) {
            try {
                in.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();
            }
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
            JSONObject jsonBird = new JSONObject();
            jsonBird.put("x", bird.x);
            jsonBird.put("y", bird.y);
            jsonBird.put("angle", bird.angle);
            jsonBird.put("lifespan", bird.lifespan);
            jsonBird.put("secondsAlive", bird.secondsAlive);
            jsonBird.put("isStuck", bird.isStuck);
            jsonBird.put("shouldCluck", bird.shouldCluck);
            bird.shouldCluck = false;
            mamaBirdsArray.put(jsonBird);
        }
        JSONArray childBirdsArray = new JSONArray();
        for (ChildBird bird : childBirds) {
            JSONObject jsonBird = new JSONObject();
            jsonBird.put("x", bird.x);
            jsonBird.put("y", bird.y);
            jsonBird.put("angle", bird.angle);
            jsonBird.put("lifespan", bird.lifespan);
            jsonBird.put("secondsAlive", bird.secondsAlive);
            jsonBird.put("isStuck", bird.isStuck);
            jsonBird.put("shouldCluck", bird.shouldCluck);
            bird.shouldCluck = false;
            childBirdsArray.put(jsonBird);
        }
        JSONArray deadBirdsArray = new JSONArray();
        for (MamaBird bird : deadBirds) {
            JSONObject jsonBird = new JSONObject();
            jsonBird.put("x", bird.x);
            jsonBird.put("y", bird.y);
            jsonBird.put("angle", bird.angle);
            jsonBird.put("lifespan", bird.lifespan);
            jsonBird.put("secondsAlive", bird.secondsAlive);
            jsonBird.put("isStuck", bird.isStuck);
            jsonBird.put("shouldCluck", bird.shouldCluck);
            bird.shouldCluck = false;
            deadBirdsArray.put(jsonBird);
        }
        rootObject.put("mamaBirds", mamaBirdsArray);
        rootObject.put("childBirds", childBirdsArray);
        rootObject.put("deadBirds", deadBirdsArray);

        return rootObject;
    }
}
