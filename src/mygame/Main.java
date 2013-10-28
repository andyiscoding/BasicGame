package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import com.jme3.ui.Picture;

/**
 * Andrew Naylor
 * CMSC 325
 * 10/27/2013
 * 
 * Homework 1
 * 
 * The Basic design for the 'Coins Up' game revolves around moving a 3D Cylinder up
 * then down and rotating all the while. The Cylinders rotation speed is partly based on user input 
 * and partly based on a seed used to throw off the Rotation speed. I was having a problem converting the radians
 * to determine which side of the coin the is showing when it lands, so I think my results are about 1/100 accurate
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    //Quaternion for starting the begining rotation of the object
    public static final Quaternion PITCH090 = new Quaternion().fromAngleAxis(FastMath.PI / 2, new Vector3f(1, 0, 0));
    
    //The static strings used to control the actionlister
    private static String CHOICE = "";
    private static String FLIP_HEADS = "Heads";
    private static String FLIP_TAILS = "Tails";
    private static String INCREASE_SPEED_ACTION = "Increase";
    private static String DECREASE_SPEED_ACTION = "Decrease";
    private static String DEBUG_ROTATE_ACTION_X_UP = "RotateIncrease";
    private static String DEBUG_ROTATE_ACTION_X_DOWN = "RotateDecrease";
    
    //The beginning values for the rotation speed and number of rotations
    public static float rotateSpeed = 2.5f;
    public static float rotationSpeedSeed = 4f;
    private static float movementCeiling = 2f;
    
    //boolean to control the flow of the game
    boolean goingUp = true;
    boolean inTheAir = false;
    boolean isRunning = true;
    
    //This is the main geometry used in the game
    protected Geometry player;
    
    //The heading for the title
    protected Picture titlePic = new Picture("TitlePic");
    private static int TITLE_PIC_WIDTH = 580;
    private static int TITLE_PIC_HEIGHT = 181;
    
    //The text for all the guiNode's text on screen
    protected BitmapText titleText;
    protected BitmapText choiceDeclaration;
    protected BitmapText headsControlText;
    protected BitmapText tailsControlText;
    protected BitmapText fasterControlText;
    protected BitmapText slowerControlText;
    protected BitmapText flipControl;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        //Going live with the fps diagnostics console disabled
        setDisplayFps(false);
        //Hiding the Statistics console too
        setDisplayStatView(false);
        //Disabling fly cam so the screen is fixed
        flyCam.setEnabled(false);
        //initializing the text on start up for a hud
        initControlText();

        //Decided to go with the Cylinder as the Mesh, and shrink down to quater like size
        Cylinder cylinder = new Cylinder(100, 100, 1, .1f, true);
        player = new Geometry("Player", cylinder);
        
        //Setting the material as a texture that reflects light differently off top and bottom
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        player.setMaterial(mat);
        rootNode.attachChild(player);
        
        //Rotating and moving the Quater so it starts off in a good spot
        player.rotate(PITCH090);
        player.move(0, -2, 0);
        
        //initializing listeneres
        initKeys();
        
        //Lighting the scene
        letThereBeLight();
    }

    /**
     *initControlText
     * 
     * This method sets all the visible text present on the viewable canvas
     * There is no logic in this function, just setting values and laying out the scene
     * 
     * 
     */
    private void initControlText() {
        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        
        //Setting the heading banner for the main scene
        titlePic.setImage(assetManager, "Textures/TitlePic.png", true);
        titlePic.setHeight(TITLE_PIC_HEIGHT);
        titlePic.setWidth(TITLE_PIC_WIDTH);
        titlePic.setPosition((settings.getWidth() / 2) - (TITLE_PIC_WIDTH / 2) + 20, settings.getHeight() - TITLE_PIC_HEIGHT);
        
        //Initializing all the Text
        choiceDeclaration = new BitmapText(guiFont, false);
        headsControlText = new BitmapText(guiFont, false);
        tailsControlText = new BitmapText(guiFont, false);
        fasterControlText = new BitmapText(guiFont, false);
        slowerControlText = new BitmapText(guiFont, false);
        flipControl = new BitmapText(guiFont, false);

        //Setting the size of the text
        choiceDeclaration.setSize(guiFont.getCharSet().getRenderedSize());
        headsControlText.setSize(guiFont.getCharSet().getRenderedSize());
        tailsControlText.setSize(guiFont.getCharSet().getRenderedSize());
        fasterControlText.setSize(guiFont.getCharSet().getRenderedSize());
        slowerControlText.setSize(guiFont.getCharSet().getRenderedSize());
        flipControl.setSize(guiFont.getCharSet().getRenderedSize());

        //Setting the text of the text
        choiceDeclaration.setText("Please Choose...");
        headsControlText.setText("Left  -  Heads");
        tailsControlText.setText("Right -  Tails");
        fasterControlText.setText("Up    -  Faster!");
        slowerControlText.setText("Down  -  Slower");
        flipControl.setText("Rotation Speed: 0");

        //Setting custom colors for the flip Control dialogue and the choiceDeclaration
        flipControl.setColor(ColorRGBA.Green);
        choiceDeclaration.setColor(ColorRGBA.Yellow);

        //Setting the positioning of the texts, I borrowed some techiniques from the JMonkeyEngine SDK 
        //however this came pretty easy given my experience with Java based web application design
        choiceDeclaration.setLocalTranslation(20, (settings.getHeight() / 2) + (headsControlText.getLineHeight() * 4), 0);
        flipControl.setLocalTranslation(settings.getWidth() - flipControl.getLineWidth() - 20, (settings.getHeight() / 4) + (flipControl.getLineHeight() * 1), 0);
        headsControlText.setLocalTranslation(settings.getWidth() - tailsControlText.getLineWidth() - 10, (settings.getHeight() / 2) + (headsControlText.getLineHeight() * 4), 0);
        tailsControlText.setLocalTranslation(settings.getWidth() - tailsControlText.getLineWidth() - 10, (settings.getHeight() / 2) + (tailsControlText.getLineHeight() * 3), 0);
        fasterControlText.setLocalTranslation(settings.getWidth() - tailsControlText.getLineWidth() - 10, (settings.getHeight() / 2) + (fasterControlText.getLineHeight() * 2), 0);
        slowerControlText.setLocalTranslation(settings.getWidth() - tailsControlText.getLineWidth() - 10, (settings.getHeight() / 2) + (slowerControlText.getLineHeight() * 1), 0);
        
        //Attaching all the kids to the guinode
        guiNode.attachChild(choiceDeclaration);
        guiNode.attachChild(flipControl);
        guiNode.attachChild(headsControlText);
        guiNode.attachChild(tailsControlText);
        guiNode.attachChild(fasterControlText);
        guiNode.attachChild(slowerControlText);
        guiNode.attachChild(titlePic);
    }

    /*
     *initKeys
     * 
     * Initializing the Key bindings for the actionlisteners
     * 
     * The Key1 and Key2 action listeneres were used for debugging purposes I have elected to leave them in
     * as is seeing as they will not break anything
     */
    private void initKeys() {
        inputManager.addMapping(FLIP_HEADS, new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping(FLIP_TAILS, new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping(INCREASE_SPEED_ACTION, new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping(DECREASE_SPEED_ACTION, new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping(DEBUG_ROTATE_ACTION_X_UP, new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping(DEBUG_ROTATE_ACTION_X_DOWN, new KeyTrigger(KeyInput.KEY_2));
        inputManager.addListener(actionListener, new String[]{FLIP_HEADS, FLIP_TAILS, INCREASE_SPEED_ACTION, DECREASE_SPEED_ACTION, DEBUG_ROTATE_ACTION_X_UP, DEBUG_ROTATE_ACTION_X_DOWN});
    }
    
    /**
     *
     * The actionlistener for all the pressable buttons 
     */
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            //Increases the rotation speed by a degree of 1, to a cieling of 10
            if (name.equals(INCREASE_SPEED_ACTION) && !isPressed) {
                if (rotateSpeed < 10f) {
                    rotateSpeed = rotateSpeed + 1f;
                }
            //Decreases the rotation speed by a degree of -1, to a cieling of -10
            } else if (name.equals(DECREASE_SPEED_ACTION) && !isPressed) {
                if (rotateSpeed > -10f) {
                    rotateSpeed = rotateSpeed - 1f;
                }
            //One of the debug tools I used to basically rotate the Geomerty and get a 
            //read out of the localRotation of the Geometry, this was used to 
            //try to refine my test cases for finding out when the coin lands on 
            //heads or tails
            } else if (name.equals(DEBUG_ROTATE_ACTION_X_UP) && !isPressed) {
                System.out.println("Local Rotation Before messing " + player.getLocalRotation());
                player.rotate(1 * (FastMath.PI * 1 / 180), 0, 0);
                System.out.println("Local Rotation After messing " + player.getLocalRotation());
            } else if (name.equals(DEBUG_ROTATE_ACTION_X_DOWN) && !isPressed) {
                System.out.println("Other Rotation Before messing " + player.getLocalRotation());
                player.rotate(FastMath.PI * 20 / 180, 0, 0);
                System.out.println("Local Rotation After messing " + player.getLocalRotation());
            //These next actionlisteners actually kick off the coin flip, the flip starts by changing
            //a control boolean to move the coin
            } else if (name.equals(FLIP_HEADS) && !isPressed) {
                //If the coin is in the air then do not go down this oath
                if (!inTheAir) {
                    //set in the air to true, later referrenced in simple update
                    inTheAir = true;
                    //Change the choiceDeclaration to the color of the side and inform
                    //the user of their choice
                    choiceDeclaration.setColor(ColorRGBA.Magenta);
                    choiceDeclaration.setText("You Chose Heads (Purple)");
                    //Set their choice globally
                    CHOICE = FLIP_HEADS;
                }
            } else if (name.equals(FLIP_TAILS) && !isPressed) {
                if (!inTheAir) {
                    inTheAir = true;
                    choiceDeclaration.setColor(ColorRGBA.Yellow);
                    choiceDeclaration.setText("You Chose Tails (Yellow)");
                    CHOICE = FLIP_TAILS;
                }
            }
            //System.out.println(rotateSpeed + " -- " + isPressed);
        }
    };

    /*
     *This is the simple update method, it controls all 
     * the logic for the coin flip
     *  -Checks if the boolean inTheAir is true
     *  -rotates and moves up until it has reached the cieling
     *  -changes goingUp to false
     *  -begins rotating and moving down until it reaches the floor
     *  -Evaluates the rotatation of the coin to evaluate Win or Lose
     *  -Informs the user of their fate
     */
    
    @Override
    public void simpleUpdate(float tpf) {
        //setting control text, so it is immediatly updated
        flipControl.setText("Rotation Speed: " + rotateSpeed);
        //changed once the left or right arrow is pressed, we have started the action
        if (inTheAir) {
            //goingUp starts as true, dictates which direction to go
            if (goingUp) {
                //rotating the X axis by (rotation speed seed(should have made this random) * user provided rotation speed * tpf) 
                player.rotate(rotationSpeedSeed * rotateSpeed * tpf, 0, 0);
                //moving the Geometry up the Y axis by the tpf, to keep the movement smooth across systems
                player.move(0, 1 * tpf, 0);
                //If we have travelled past the movementCeiling
                if (player.getLocalTranslation().getY() > movementCeiling) {
                    //gravity ensues
                    goingUp = false;
                }
            //Now we are going down
            } else {
                //Keep on rotating at the same speed
                player.rotate(rotationSpeedSeed * rotateSpeed * tpf, 0, 0);
                //moving the Geometry down
                player.move(0, -1 * tpf, 0);
                //If we have travelled below the movementCeiling
                if (player.getLocalTranslation().getY() < (-1 * movementCeiling)) {
                    //Setting the final laying radian of the W axis to evaluate which side of the coin we are on
                    float finalW_Axis_Radian = player.getLocalRotation().getW();
                    //If the finalW Axis was between .641 and -.636 I determined the Yellow side of the coin was showing
                    if (finalW_Axis_Radian < .641f && finalW_Axis_Radian > -.636f) {
                        //If the user chose Tails (Yellow)
                        if (CHOICE == FLIP_TAILS) {
                            //They are certaintly winner winners
                            chickenDinner("Yellow");
                        } else {
                            //They have failed this game of probability
                            maybeNextTime("Yellow");
                        }
                    //This range was a bit more problematic to calculate the acceptable range was: (1.5 > headsRange > .642 && -1.5 < headsRange < -.642) 
                    } else if ((finalW_Axis_Radian > .642f && finalW_Axis_Radian < 1.5f) || (finalW_Axis_Radian < -.642f && finalW_Axis_Radian > -1.5f)) {
                        if (CHOICE == FLIP_HEADS) {
                            chickenDinner("Purple");
                        } else {
                            maybeNextTime("Purple");
                        }
                    //These ranges were supposed to be where no color was showing...i.e how a coin would land flat
                    }else{
                        indertiminationStation(finalW_Axis_Radian + " ");
                    }
                    //Recalibrating the booleans, time to let the player play again
                    goingUp = true;
                    inTheAir = false;
                }
            }
        }
    }
   /**
    * The case where the coin would land with no color showing
    */
    public void indertiminationStation(String choice){
        choiceDeclaration.setColor(ColorRGBA.Gray);
        choiceDeclaration.setText("Yeh....this happens sometimes, \nlanded on side, please reflip " + choice);
    }
    /**
     * The user has correctly predicted the coin fall
     */
    public void chickenDinner(String choice) {
        choiceDeclaration.setColor(ColorRGBA.Green);
        choiceDeclaration.setText("Congrats you win!! You should get cake! \n\n It was " + choice);
    }
    /**
     * The user has correctly predicted the coin fall
     */
    public void maybeNextTime(String choice) {
        choiceDeclaration.setColor(ColorRGBA.Red);
        choiceDeclaration.setText("Awwww Shucks...Maybe Next Time \n\n It was " + choice);
    }

    public void moveCube(int delta) {
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    /**
     * Setting an ambient light for the lightsource
     * required for the Texture I used
     */
    public void letThereBeLight() {
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White);
        rootNode.addLight(ambient);
    }
}