package dev.m8u.dubkovlabsserver;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Henhouse {

    int canvasWidth = 485, canvasHeight = 559;

    Instant startTime;

    Timer timer;
    long currentSec; //

    int frameCount;

    Random random;
    float currentRandomFloat;

    int N1 = 3, N2 = 1, birdsLifespan = 30; //
    float K = 0.5f, P = 0.5f;//

    long mamaLastSec = -1, childLastSec = -1;
    long randomFloatLastSec = -1;

    ArrayList<MamaBird> mamaBirds;//
    ArrayList<ChildBird> childBirds;//
    ArrayList<MamaBird> deadBirds;//

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
            if (currentSec % N1 == 0 && currentRandomFloat <= P) {
                mamaBirds.add(new MamaBird(170 + random.nextInt(canvasWidth - 170*2),
                        170 + random.nextInt(canvasHeight - 170*2),
                        1 + random.nextInt(2),
                        1 + random.nextInt(2),
                        0,
                        1 + random.nextInt(3),
                        (int) (birdsLifespan / 3.0f)));
            }
            mamaLastSec = currentSec;
        }
        if (currentSec != childLastSec) {
            for (ChildBird bird : childBirds) {
                bird.secondsAlive++;
            }
            if (currentSec % N2 == 0 && (float) childBirds.size() / mamaBirds.size() < K) {
                childBirds.add(new ChildBird((canvasWidth/2 + (random.nextInt(50) - 25)),
                        (canvasHeight/2 + (random.nextInt(50) - 25)),
                        2 + random.nextInt(5),
                        2 + random.nextInt(5),
                        0,
                        6 + random.nextInt(10)));
                childLastSec = currentSec;

            }
            childLastSec = currentSec;
        }

        for (Iterator<MamaBird> iterator = mamaBirds.iterator(); iterator.hasNext();) {
            MamaBird bird = iterator.next();
            if (bird.secondsAlive > birdsLifespan) {
                iterator.remove();
                deadBirds.add(new MamaBird(bird.x, bird.y, 0, -4, 0, 0,31));

            }
            bird.animationStep();
            if (bird.checkForBorders(canvasWidth, canvasHeight) && bird.collisionsInRow < 50) {
                bird.shouldCluck = true;
            }
        }
        for (Iterator<ChildBird> iterator = childBirds.iterator(); iterator.hasNext();) {
            ChildBird bird = iterator.next();
            if (bird.secondsAlive >= birdsLifespan / 3.0f) {
                iterator.remove();

                mamaBirds.add(new MamaBird(bird.x, bird.y,
                        (bird.xVel > 0 ? 1 + random.nextInt(2) : (1 + random.nextInt(2)) * -1),
                        (bird.yVel > 0 ? 1 + random.nextInt(2) : 1 + (random.nextInt(2)) * -1),
                        bird.angle,
                        (bird.angleVel > 0 ? 1 + random.nextInt(3) : (1 + random.nextInt(3)) * -1),
                        (int) (birdsLifespan / 3.0f)));

            }
            if (bird.secondsAlive >= birdsLifespan / 6.0f)
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
    //{"currentSec": value, "N1": value, "N2": value, "K": value, "P": value, "birdsLifespan": value, "mamaBirds": [{}, {}, ...], "childBirds": [{}, {}, ...], "deadBirds": [{}, {}, ...]}

    JSONObject getJSON() {
        JSONObject rootObject = new JSONObject();
        rootObject.put("currentSec", currentSec);
        rootObject.put("N1", N1);
        rootObject.put("N2", N2);
        rootObject.put("K", K);
        rootObject.put("P", P);
        rootObject.put("birdsLifespan", birdsLifespan);
        JSONArray mamaBirdsArray = new JSONArray();
        for (MamaBird bird : mamaBirds) {
            JSONObject jsonBird = new JSONObject();
            jsonBird.put("x", bird.x);
            jsonBird.put("y", bird.y);
            jsonBird.put("angle", bird.angle);
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