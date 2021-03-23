import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.awt.*;

@ScriptManifest(name = "arab's Sapphire ring crafter",
        description = "",
        author = "arab god",
        version = 1.1,
        category = Category.CRAFTING,
        image = "jbkqYi1.png")


public class main extends AbstractScript {

    Timer timer = new Timer();

    Area GE = new Area(3161, 3489, 3168, 3486, 0);
    Area EDGE_FURNACE = new Area(3107, 3500, 3109, 3497, 0);
    Area EDGE_BANK = new Area(3096, 3496, 3098, 3494, 0);

    int ringsCrafted = 0;
    int craftingLvl = 1;
    int gpAmount = 0;
    int goldbarAmount = 0;
    int sapphireAmount = 0;
    int leatherAmount = 0;
    int needleAmount = 0;
    int threadAmount = 0;
    int ringMould = 0;
    int amuletMould = 0;
    int leatherRequired = 0;

    // buy only
    int sapphirePrice = 325;
    int goldbarPrice = 80;
    int leatherPrice = 160;

    // sell only
    int ringsPrice = 480;


    boolean debugMode = true;

    public enum Action {
        CHECKING,
        CRAFT_SAPPHIRE,
        CRAFT_AMULET,
        CRAFT_GLOVES
    }
    Action action = Action.CHECKING;


    @Override
    public void onPaint(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("sapphire ring crafter", 20, 30);

        g.setColor(Color.WHITE);
        g.drawString("Runtime: " + timer.formatTime(), 20, 50);
        g.drawString("Status: " + action, 20, 70);
        g.drawString("Rings crafted: " + ringsCrafted, 20, 90);
        g.drawString("Rings/h: " + timer.getHourlyRate(ringsCrafted), 20, 110);
        g.drawString("Profit: " + ringsCrafted * (ringsPrice - (goldbarPrice + sapphirePrice)), 20, 130);
        g.drawString("Profit/h: " + timer.getHourlyRate(ringsCrafted * (ringsPrice - (goldbarPrice + sapphirePrice))) / 1000 + "k", 20, 150);
        g.drawString("Crafting lvl: " + craftingLvl, 20, 170);
        g.drawString("Crafting xp/h: " + timer.getHourlyRate(ringsCrafted * 40), 20, 190);

        if (debugMode) {
            g.setFont(new Font("Arial", Font.PLAIN, 15));

            g.drawString("Action: " + action, 20, 210);
            g.drawString("craftingLvl: " + craftingLvl, 20, 225);
            g.drawString("gpAmount: " + gpAmount, 20, 240);
            g.drawString("sapphireAmount: " + sapphireAmount, 20, 255);
            g.drawString("goldbarAmount: " + goldbarAmount, 20, 270);
            g.drawString("leatherAmount: " + leatherAmount, 20, 285);
            g.drawString("threadAmount: " + threadAmount, 20, 300);
            g.drawString("needleAmount: " + needleAmount, 20, 315);
            g.drawString("ringMould: " + ringMould, 20, 330);
            g.drawString("amuletMould: " + amuletMould, 20, 345);
            g.drawString("leatherRequired" + leatherRequired, 20, 360);
        }
    }

    public int onLoop() {

        switch (action) {

            case CHECKING:

                craftingLvl = Skills.getRealLevel(Skill.CRAFTING);

                if (Bank.openClosest()) {

                    sleep(300, 500);

                    gpAmount = Bank.count("Coins") + Inventory.count("Coins");
                    goldbarAmount = Bank.count("Gold bar") + Inventory.count("Gold bar");
                    sapphireAmount = Bank.count("Sapphire") + Inventory.count("Sapphire");
                    ringMould = Bank.count("Ring mould") + Inventory.count("Ring mould");
                    amuletMould = Bank.count("Amulet mould") + Inventory.count("Amulet mould");
                    leatherAmount = Bank.count("Leather") + Inventory.count("Leather");
                    needleAmount = Bank.count("Needle") + Inventory.count("Needle");
                    threadAmount = Bank.count("Thread") + Inventory.count("Thread");

                    sleep(300, 500);
                    Bank.close();
                    sleep(1000, 1500);
                    if (craftingLvl < 8) {
                        action = Action.CRAFT_GLOVES;
                    } else if (craftingLvl < 20) {
                        action = Action.CRAFT_AMULET;
                    } else if (craftingLvl >= 20) {
                        action = Action.CRAFT_SAPPHIRE;
                    }
                }


                break;


            case CRAFT_GLOVES:
                // (experience needed - current experience) divided by how much experience 1 leather gives
                int leatherRequired = (Skills.getExperienceForLevel(8) - Skills.getExperience(Skill.CRAFTING)) / 13;

                if (leatherAmount < leatherRequired
                        || threadAmount < leatherRequired / 5
                        || needleAmount < 1) {

                    if (!GE.contains(getLocalPlayer())) {
                        MethodProvider myPlayer = new MethodProvider();
                        while (!GE.contains(myPlayer.getLocalPlayer())) {
                            Walking.walk(GE.getRandomTile());
                            sleep(2000, 2500);
                        }
                    }

                    if (Bank.openClosest()) {
                        sleep(300, 500);
                        Bank.withdrawAll("Coins");
                        sleep(300, 500);
                        Bank.close();
                    }

                    if (GrandExchange.open()) {
                        sleep(1000, 1500);

                        if (leatherAmount < leatherRequired) {
                            if (GrandExchange.buyItem("Leather", leatherRequired - leatherAmount, leatherPrice)) {
                                if (sleepUntil(GrandExchange::isReadyToCollect, 15000)) {
                                    GrandExchange.collect();
                                    leatherAmount += leatherRequired - leatherAmount;
                                }
                            }
                            sleep(1000, 1500);
                        }

                        if (threadAmount < leatherRequired / 5) {
                            if (GrandExchange.buyItem("Thread", (leatherRequired - leatherAmount) / 5, 10)) {
                                if (sleepUntil(GrandExchange::isReadyToCollect, 15000)) {
                                    GrandExchange.collect();
                                    threadAmount += (leatherRequired - leatherAmount) / 5;
                                }
                            }
                            sleep(1000, 1500);
                        }

                        if (needleAmount < 1) {
                            if (GrandExchange.buyItem("Needle", 1, 200)) {
                                if (sleepUntil(GrandExchange::isReadyToCollect, 15000)) {
                                    GrandExchange.collect();
                                    needleAmount += 1;
                                }
                            }
                            sleep(1000, 1500);
                        }
                        if (GrandExchange.isOpen()) {
                            GrandExchange.close();
                            sleep(300, 500);
                        }
                    }
                    if (Bank.openClosest()) {
                        sleep(300, 500);
                        Bank.depositAllItems();
                        Bank.withdraw("Needle", 1);
                        Bank.withdraw("Thread", 12);
                        sleep(300, 500);
                        Bank.close();
                    }
                }
                while (craftingLvl < 8) {

                    if (!Inventory.contains("Leather") || !Inventory.contains("Thread") || !Inventory.contains("Needle")) {
                        if (Bank.openClosest()) {
                            sleep(300, 500);
                            Bank.depositAllExcept("Leather", "Needle", "Thread");
                            sleep(300, 500);

                            if (Inventory.contains("Leather gloves")) {
                                Bank.depositAll("Leather gloves");
                            }
                            sleep(300, 500);

                            if (!Inventory.contains("Needle")) {
                                Bank.withdraw("Needle", 1);
                            }
                            sleep(300, 500);

                            if (!Inventory.contains("Thread")) {
                                Bank.withdrawAll("Thread");
                            }
                            sleep(300, 500);

                            if (!Inventory.contains("Leather")) {
                                Bank.withdrawAll("Leather");
                            }
                            sleep(300, 500);
                            Bank.close();
                        }
                    }

                    if (!Bank.isOpen()) {
                        Inventory.interact("Needle", "Use");
                        sleep(1500, 2000);
                        Inventory.interact("Leather", "Use");
                        sleep(1500, 2000);
                        WidgetChild LeatherGloves = Widgets.getWidget(270).getChild(14);
                        if (LeatherGloves != null) {
                            LeatherGloves.interact();
                            sleep(1000, 3000);
                        }

                        sleepUntil(() -> !Inventory.contains("Leather") || Dialogues.canContinue(), 60000);
                        if (Dialogues.canContinue()) {
                            Dialogues.spaceToContinue();
                            craftingLvl = Skills.getRealLevel(Skill.CRAFTING);
                        }
                    }
                }
                if (craftingLvl >= 8) {
                    action = Action.CRAFT_AMULET;
                }

                break;


            case CRAFT_AMULET:
                if (GrandExchange.isOpen()) {
                    GrandExchange.close();
                } else if (Bank.isOpen()) {
                    Bank.close();
                }
                sleep(300, 500);

                if (craftingLvl <= 10 && (goldbarAmount < 125 || amuletMould < 1)) {

                    if (!GE.contains(getLocalPlayer())) {
                        MethodProvider myPlayer = new MethodProvider();
                        while (!GE.contains(myPlayer.getLocalPlayer())) {
                            Walking.walk(GE.getRandomTile());
                            sleep(2000, 2500);
                        }
                    }
                    if (Bank.openClosest()) {
                        sleep(300, 500);
                        Bank.withdrawAll("Coins");
                        sleep(300, 500);
                        Bank.close();
                    }
                    if (GrandExchange.open()) {
                        sleep(1000, 1500);
                        if (goldbarAmount < 125) {
                            if (GrandExchange.buyItem("Gold bar", 125 - goldbarAmount, goldbarPrice)) {
                                if (sleepUntil(GrandExchange::isReadyToCollect, 15000)) {
                                    GrandExchange.collect();
                                    goldbarAmount += 125;
                                }
                            }
                            sleep(1000, 1500);
                        }
                        if (amuletMould < 1) {
                            if (GrandExchange.buyItem("Amulet mould", 1 - amuletMould, 100)) {
                                if (sleepUntil(GrandExchange::isReadyToCollect, 15000)) {
                                    GrandExchange.collect();
                                    amuletMould++;
                                }
                            }
                            sleep(1000, 1500);
                        }
                        GrandExchange.close();
                    }
                    if (Bank.openClosest()) {
                        sleep(300, 500);
                        Bank.depositAllItems();
                        gpAmount = Bank.count("Coins");
                        goldbarAmount = Bank.count("Gold bar");
                        amuletMould = Bank.count("Amulet mould");
                        sleep(300, 500);
                        Bank.close();
                    }
                }
                if (!EDGE_BANK.contains(getLocalPlayer())) {
                    MethodProvider myPlayer = new MethodProvider();
                    while (!EDGE_BANK.contains(myPlayer.getLocalPlayer())) {
                        Walking.walk(EDGE_BANK.getRandomTile());
                        sleep(2000, 2500);
                    }
                }
                while (craftingLvl < 20) {

                    if (!Inventory.contains("Amulet mould")) {
                        if (!Bank.isOpen()) {
                            Bank.openClosest();
                        }
                        sleep(300, 500);
                        Bank.withdraw("Amulet mould", 1);
                    }

                    if (Inventory.contains("Gold amulet (u)")) {
                        if (!Bank.isOpen()) {
                            Bank.openClosest();
                        }
                        sleep(300, 500);
                        Bank.depositAll("Gold amulet (u)");
                    }

                    if (!Inventory.contains("Gold bar")) {
                        if (!Bank.isOpen()) {
                            Bank.openClosest();
                        }
                        sleep(300, 500);
                        Bank.withdrawAll("Gold bar");
                    }
                    sleep(300, 500);

                    if (Bank.isOpen()) {
                        Bank.close();
                        sleep(300, 500);
                    }
                    GameObject Furnace = GameObjects.closest("Furnace");

                    if (Furnace != null && Furnace.hasAction("Smelt")) {
                        Furnace.interact("Smelt");
                        sleep(300, 500);
                        sleepUntil(() -> !getLocalPlayer().isMoving(), 10000);

                        if (EDGE_FURNACE.contains(getLocalPlayer())) {
                            if (Dialogues.canContinue()) {
                                Dialogues.spaceToContinue();
                                sleep(800, 1000);
                            }
                            if (Widgets.getWidget(446).getChild(34) != null) {
                                WidgetChild smeltGoldAmulet = Widgets.getWidget(446).getChild(34);

                                if (smeltGoldAmulet != null) {
                                    sleep(300, 500);
                                    smeltGoldAmulet.interact();
                                    sleepUntil(() -> !Inventory.contains("Gold bar") || Dialogues.canContinue(), 50000);
                                    if (Dialogues.canContinue()) {
                                        Dialogues.spaceToContinue();
                                        craftingLvl = Skills.getRealLevel(Skill.CRAFTING);
                                    }
                                }
                            }
                        }
                    }
                    if (craftingLvl >= 20) {
                        action = Action.CRAFT_SAPPHIRE;
                    }
                }
                break;


            case CRAFT_SAPPHIRE:

                sleep (1000, 1500);
                if (goldbarAmount < 13 || sapphireAmount < 13 || ringMould < 1) {
                    //buy materials
                    if (Bank.isOpen()) {
                        Bank.close();
                        sleep(300, 500);
                    }
                    if (!GE.contains(getLocalPlayer())) {
                        MethodProvider myPlayer = new MethodProvider();
                        while (!GE.contains(myPlayer.getLocalPlayer())) {
                            Walking.walk(GE.getRandomTile());
                            sleep(2000, 2500);
                        }
                    } else {
                        if (Bank.openClosest()) {
                            sleep(300, 500);
                            gpAmount = Bank.count("Coins") + Inventory.count("Coins");
                            goldbarAmount = Bank.count("Gold bar") + Inventory.count("Gold bar");
                            sapphireAmount = Bank.count("Sapphire") + Inventory.count("Sapphire");
                            ringMould = Bank.count("Ring mould") + Inventory.count("Ring mould");

                            Bank.depositAllExcept("Coins");
                            sleep(300, 500);
                            if (Bank.contains("Coins")) {
                                Bank.withdrawAll("Coins");
                                sleep(300, 500);
                            }
                            if (Bank.contains("Sapphire ring")) {
                                Bank.setWithdrawMode(BankMode.NOTE);
                                sleep(300, 500);
                                Bank.withdrawAll("Sapphire ring");
                                sleep(300, 500);
                                Bank.setWithdrawMode(BankMode.ITEM);
                                sleep(300, 500);
                            }
                            Bank.close();

                            if (GrandExchange.open()) {
                                sleep(1000, 1500);
                                if (Inventory.contains("Sapphire ring")) {
                                    if (GrandExchange.sellItem("Sapphire Ring", Inventory.count("Sapphire ring"), ringsPrice)) {
                                        if (sleepUntil(GrandExchange::isReadyToCollect, 15000)) {
                                            GrandExchange.collect();
                                            sleep(1000, 1500);
                                        }
                                    }
                                }

                                if (ringMould == 0) {
                                    if (GrandExchange.buyItem("Ring mould", 1, 200)) {
                                        if (sleepUntil(GrandExchange::isReadyToCollect, 15000)) {
                                            GrandExchange.collect();
                                            sleep(1000, 1500);
                                        }
                                    }
                                }

                                if (Inventory.count("Coins") >= (sapphirePrice + goldbarPrice) * 1060) {
                                    if (GrandExchange.buyItem("Gold bar", 1060, goldbarPrice)) {
                                        if (sleepUntil(GrandExchange::isReadyToCollect, 15000)) {
                                            GrandExchange.collect();
                                            goldbarAmount += 1060;
                                            sleep(1000, 1500);
                                        }
                                    }
                                    if (GrandExchange.buyItem("Sapphire", 1060, sapphirePrice)) {
                                        if (sleepUntil(GrandExchange::isReadyToCollect, 15000)) {
                                            GrandExchange.collect();
                                            sapphireAmount += 1060;
                                            sleep(1000, 1500);
                                        }
                                    }
                                } else if (Inventory.count("Coins") >= 100000) {
                                    gpAmount = Inventory.count("Coins");
                                    int amount = gpAmount / (goldbarPrice + sapphirePrice);
                                    if (GrandExchange.buyItem("Gold bar", amount, goldbarPrice)) {
                                        if (sleepUntil(GrandExchange::isReadyToCollect, 15000)) {
                                            GrandExchange.collect();
                                            goldbarAmount += amount;
                                            sleep(1000, 1500);
                                        }
                                    }
                                    if (GrandExchange.buyItem("Sapphire", amount, sapphirePrice)) {
                                        if (sleepUntil(GrandExchange::isReadyToCollect, 15000)) {
                                            GrandExchange.collect();
                                            sapphireAmount += amount;
                                            sleep(1000, 1500);
                                        }
                                    }
                                }
                                gpAmount = Inventory.count("Coins");
                                GrandExchange.close();
                                sleep(300, 500);
                            }
                            if (Bank.openClosest()) {
                                sleep(300, 500);
                                Bank.depositAllItems();
                                sleep(300, 500);
                                Bank.withdraw("Ring mould");
                                sleep(300, 500);
                                Bank.close();
                            }
                        }
                    } // finish buying


                } // else start crafting
                else {
                    if (!EDGE_BANK.contains(getLocalPlayer()) && !EDGE_FURNACE.contains(getLocalPlayer())) {
                        MethodProvider myPlayer = new MethodProvider();
                        while (!EDGE_BANK.contains(myPlayer.getLocalPlayer())) {
                            Walking.walk(EDGE_BANK.getRandomTile());
                            sleep(2000, 2500);
                        }
                    }

                    if (!Inventory.contains("Gold bar")
                            || !Inventory.contains("Sapphire")
                            || !Inventory.contains("Ring mould")
                            || Inventory.contains("Sapphire ring")) {

                        if (Bank.openClosest()) {
                            ringsCrafted += Inventory.count("Sapphire ring");

                            Bank.depositAllExcept("Ring mould", "Gold bar", "Sapphire");
                            sapphireAmount = Bank.count("Sapphire");
                            goldbarAmount = Bank.count("Gold bar");
                            sleep(300, 500);
                            if (!Inventory.contains("Ring mould")) {
                                Bank.withdraw("Ring mould", 1);
                                sleep(300, 500);
                            }
                            if (!Inventory.contains("Gold bar")) {
                                Bank.withdraw("Gold bar", 13);
                                goldbarAmount -= 13;
                                sleep(300, 500);
                            }
                            if (!Inventory.contains("Sapphire")) {
                                Bank.withdraw("Sapphire", 13);
                                sapphireAmount -= 13;
                                sleep(300, 500);
                            }
                            Bank.close();
                            sleep(300, 500);
                        }
                    } else if (Inventory.contains("Gold bar") && Inventory.contains("Sapphire") && Inventory.contains("Ring mould")) {

                        if (Bank.isOpen()) {
                            Bank.close();
                            sleep(300, 500);
                        }
                        GameObject Furnace = GameObjects.closest("Furnace");

                        if (Furnace != null && Furnace.hasAction("Smelt")) {
                            Furnace.interact("Smelt");
                            sleep(300, 500);
                            sleepUntil(() -> !getLocalPlayer().isMoving(), 10000);

                            if (EDGE_FURNACE.contains(getLocalPlayer())) {
                                if (Dialogues.canContinue()) {
                                    Dialogues.spaceToContinue();
                                    sleep(800, 1000);
                                }
                                if (Widgets.getWidget(446).getChild(8) != null) {
                                    WidgetChild smeltSapphireRing = Widgets.getWidget(446).getChild(8);

                                    if (smeltSapphireRing != null) {
                                        sleep(300, 500);
                                        smeltSapphireRing.interact();
                                        sleepUntil(() -> !Inventory.contains("Gold bar") || Dialogues.canContinue(), 50000);
                                        if (Dialogues.canContinue()) {
                                            Dialogues.spaceToContinue();
                                            craftingLvl = Skills.getRealLevel(Skill.CRAFTING);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                break;
        }
        return Calculations.random(500, 1000);
    }
}