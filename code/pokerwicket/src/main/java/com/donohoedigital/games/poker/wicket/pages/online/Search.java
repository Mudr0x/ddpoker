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
package com.donohoedigital.games.poker.wicket.pages.online;

import com.donohoedigital.base.*;
import com.donohoedigital.games.poker.model.*;
import com.donohoedigital.games.poker.service.*;
import com.donohoedigital.games.poker.wicket.*;
import com.donohoedigital.games.poker.wicket.panels.*;
import com.donohoedigital.wicket.annotations.*;
import com.donohoedigital.wicket.behaviors.*;
import com.donohoedigital.wicket.common.*;
import com.donohoedigital.wicket.components.*;
import com.donohoedigital.wicket.labels.*;
import com.donohoedigital.wicket.models.*;
import com.donohoedigital.wicket.panels.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.*;
import org.apache.wicket.markup.repeater.*;
import org.apache.wicket.spring.injection.annot.*;
import org.wicketstuff.annotation.mount.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: donohoe
 * Date: May 1, 2008
 * Time: 1:36:58 PM
 * To change this template use File | Settings | File Templates.
 */
@MountPath(path = "search")
@MountFixedMixedParam(parameterNames = {Search.PARAM_SEARCH, Search.PARAM_PAGE, Search.PARAM_SIZE})
public class Search extends OnlinePokerPage
{
    private static final long serialVersionUID = 42L;

    //private static Logger logger = Logger.getLogger(Search.class);

    public static final String PARAM_SEARCH = "search";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_SIZE = "s";

    public static final int ITEMS_PER_PAGE = 10;

    @SuppressWarnings({"NonSerializableFieldInSerializableClass"})
    @SpringBean
    private OnlineProfileService profileService;

    public Search()
    {
        this(new PageParameters());
    }

    public Search(PageParameters params)
    {
        super(params);
        init(params);
    }

    private void init(PageParameters params)
    {
        String text = params.getString(PARAM_SEARCH);
        if (text != null && text.length() == 0) text = null;
        final TextField<String> name = new TextField<String>("name", new StringModel(text));
        name.add(new DefaultFocus());

        // form
        Form<?> form = new StatelessForm<Void>("form")
        {
            private static final long serialVersionUID = 42L;

            @Override
            protected void onSubmit()
            {
                PageParameters p = new PageParameters();
                p.put(PARAM_SEARCH, name.getModelObject());
                setResponsePage(Search.class, p);

                // testing of error page
                if ("__ERROR__".equals(name.getModelObject()))
                {
                    throw new ApplicationError("TESTING ERROR");
                }
            }
        };
        add(form);

        form.add(name);

        // games
        SearchData data = new SearchData(text);
        data.processSizeFromParams(params, PARAM_SIZE);

        GameListTableView dataView = new GameListTableView("row", data);
        add(dataView.setVisible(text != null));
        add(new BookmarkablePagingNavigator("navigator", dataView, new BasicPluralLabelProvider("player", "players"), getClass(), params,
                                            PARAM_PAGE));

        // no results found
        add(new StringLabel("term", text).setVisible(text != null && data.isEmpty()));
    }

    ////
    //// List
    ////

    private class SearchData extends PageableServiceProvider<OnlineProfile>
    {
        private static final long serialVersionUID = 42L;

        private String search;

        private SearchData(String search)
        {
            this.search = search;
        }

        @Override
        public Iterator<OnlineProfile> iterator(int first, int pagesize)
        {
            if (search == null || search.length() == 0) return new ArrayList<OnlineProfile>(0).iterator();
            return profileService.getMatchingOnlineProfiles(size(), first, pagesize, search, null, null, true).iterator();
        }

        @Override
        public int calculateSize()
        {
            if (search == null || search.length() == 0) return 0;
            return profileService.getMatchingOnlineProfilesCount(search, null, null, true);
        }

        public String getSearch()
        {
            return search;
        }

        public void setSearch(String s)
        {
            search = s;
        }
    }

    /**
     * The leaderboard table
     */
    private class GameListTableView extends CountDataView<OnlineProfile>
    {
        private static final long serialVersionUID = 42L;

        private GameListTableView(String id, SearchData data)
        {
            super(id, data, ITEMS_PER_PAGE);
        }

        @Override
        protected void populateItem(Item<OnlineProfile> row)
        {
            OnlineProfile profile = row.getModelObject();
            String search = getSearchData().getSearch();

            // CSS class
            row.add(new AttributeModifier("class", true,
                                          new StringModel(PokerSession.isLoggedInUser(profile.getName()) ? "highlight" :
                                                          row.getIndex() % 2 == 0 ? "odd" : "even")));

            // link to player details
            Link<?> link = History.getHistoryLink("playerLink", profile.getName());
            row.add(link);

            // player name (in link)
            link.add(new HighlightLabel("name", search, PokerWicketApplication.SEARCH_HIGHLIGHT, true));
            row.add(new WebMarkupContainer("retired").setVisible(profile.isRetired()));

            // player list
            row.add(new HighightedAliases("aliases", new PokerUser(profile),
                                          search, PokerWicketApplication.SEARCH_HIGHLIGHT));
        }

        private SearchData getSearchData()
        {
            return (SearchData) getDataProvider();
        }
    }
}