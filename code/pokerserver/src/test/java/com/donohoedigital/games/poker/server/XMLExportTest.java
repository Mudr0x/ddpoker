/*
 * =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
 * DD Poker - Source Code
 * Copyright (c) 2003-2024 Doug Donohoe
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For the full License text, please see the LICENSE.txt file
 * in the root directory of this project.
 * 
 * The "DD Poker" and "Donohoe Digital" names and logos, as well as any images, 
 * graphics, text, and documentation found in this repository (including but not
 * limited to written documentation, website content, and marketing materials) 
 * are licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 
 * 4.0 International License (CC BY-NC-ND 4.0). You may not use these assets 
 * without explicit written permission for any uses not covered by this License.
 * For the full License text, please see the LICENSE-CREATIVE-COMMONS.txt file
 * in the root directory of this project.
 * 
 * For inquiries regarding commercial licensing of this source code or 
 * the use of names, logos, images, text, or other assets, please contact 
 * doug [at] donohoe [dot] info.
 * =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
 */
package com.donohoedigital.games.poker.server;

import com.donohoedigital.config.*;
import com.donohoedigital.games.config.*;
import com.donohoedigital.games.poker.model.*;
import com.donohoedigital.xml.*;
import com.donohoedigital.base.*;
//import com.thoughtworks.xstream.*;
import junit.framework.*;
import org.apache.log4j.*;

import java.util.*;

/**
 * @author Doug Donohoe
 */
public class XMLExportTest extends TestCase
{
    private static Logger logger = Logger.getLogger(XMLExportTest.class);

//    public void testXML()
//    {
//        XStream xstream = new XStream();
//        OnlineGame game = PokerTestData.createOnlineGame("Obama", 1, "XYZ-123");
//        String toXML = xstream.toXML(game);
//        //logger.debug("GAME: " + toXML + "\n");
//
//        TournamentHistory hist = PokerTestData.createTournamentHistory("<test1>\n\"&two??", 1, "xyz");
//        xstream.useAttributeFor(TournamentHistory.class, "playerName");
//        xstream.omitField(TournamentHistory.class, "onlineGame");
//        xstream.omitField(TournamentHistory.class, "profile");
//        xstream.alias("history", TournamentHistory.class);
//        toXML = xstream.toXML(hist);
//        logger.debug("HIST: " + toXML);
//
//    }

    public void testSimpleXMLEncoder()
    {
        // setup config manager (so we know where profiles live)
        new ConfigManager("poker", ApplicationType.COMMAND_LINE);
        Utils.setVersionString("3");
        List<String> testPlayers = Arrays.asList("Doug", "Greg", "<bracket man2>", "\"Quote Man 2\"");

        // encoder
        SimpleXMLEncoder encoder = new SimpleXMLEncoder();

        // get all tp
        List<BaseProfile> profiles = TournamentProfile.getProfileList();
        int x = 0;
        for (BaseProfile p : profiles)
        {
            x++;
            TournamentProfile tp = (TournamentProfile) p;

            logger.debug("Profile: " + p.getName() + " at " + p.getFile().getAbsolutePath());

            // load rest of data
            tp.load(true);

            // set some players
            tp.setPlayers(testPlayers);

            // create game and set profile
            OnlineGame game = PokerTestData.createOnlineGame("Host " + x, x, "XYZ-123");
            game.setTournament(tp);
            game.setId(34343L);

            // results
            List<TournamentHistory> hists = new ArrayList<TournamentHistory>();
            for (int i = 1; i < 5; i++)
            {
                hists.add(PokerTestData.createTournamentHistory(game, null, "Player " + i, TournamentHistory.PLAYER_TYPE_ONLINE));
            }
            game.setHistories(hists);

            // encode            
            game.encodeXML(encoder);
        }

        logger.debug("GAMES: \n" + encoder);

    }
}
