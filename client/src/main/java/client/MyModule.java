/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client;

import client.avatar.AvatarGenerator;
import client.avatar.AvatarGeneratorImpl;
import client.data.ClientData;
import client.data.ClientDataImpl;
import client.game.Game;
import client.game.GameImpl;
import client.scenes.GameOverCtrl;
import client.scenes.MainCtrl;
import client.scenes.leaderboards.LeaderboardCtrl;
import client.scenes.leaderboards.TempLeaderboardCtrl;
import client.scenes.menus.GameModeSelectionCtrl;
import client.scenes.menus.HomeCtrl;
import client.scenes.menus.MultiplayerMenuCtrl;
import client.scenes.menus.WaitingCtrl;
import client.scenes.questions.EnergyAlternativeQuestionCtrl;
import client.scenes.questions.EstimationQuestionCtrl;
import client.scenes.questions.GameMCQCtrl;
import client.utils.ClientUtils;
import client.utils.ClientUtilsImpl;
import client.joker.JokerUtils;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

public class MyModule implements Module {

    @Override
    public void configure(Binder binder) {
        // Binding controllers
        binder.bind(MainCtrl.class).in(Scopes.SINGLETON);
        binder.bind(HomeCtrl.class).in(Scopes.SINGLETON);
        binder.bind(LeaderboardCtrl.class).in(Scopes.SINGLETON);
        binder.bind(EstimationQuestionCtrl.class).in(Scopes.SINGLETON);
        binder.bind(GameMCQCtrl.class).in(Scopes.SINGLETON);
        binder.bind(GameOverCtrl.class).in(Scopes.SINGLETON);
        binder.bind(GameModeSelectionCtrl.class).in(Scopes.SINGLETON);
        binder.bind(MultiplayerMenuCtrl.class).in(Scopes.SINGLETON);
        binder.bind(TempLeaderboardCtrl.class).in(Scopes.SINGLETON);
        binder.bind(WaitingCtrl.class).in(Scopes.SINGLETON);
        binder.bind(EnergyAlternativeQuestionCtrl.class).in(Scopes.SINGLETON);
        binder.bind(JokerUtils.class).in(Scopes.SINGLETON);

        // Binding interfaces to concrete implementations
        binder.bind(ClientUtils.class).to(ClientUtilsImpl.class).in(Scopes.SINGLETON);
        binder.bind(ClientData.class).to(ClientDataImpl.class).in(Scopes.SINGLETON);
        binder.bind(Game.class).to(GameImpl.class).in(Scopes.SINGLETON);
        binder.bind(AvatarGenerator.class).to(AvatarGeneratorImpl.class).in(Scopes.SINGLETON);
    }
}