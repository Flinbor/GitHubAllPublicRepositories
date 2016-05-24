/*
 * Copyright 2016 Flinbor Bogdanov Oleksandr
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package in.flinbor.github.publicRepositories.presenter.vo;

/**
 * View Object
 * Adapted object for UI
 */
public class Repository {
    private int    serverRepoId;
    private String repoName;
    private String description;
    private String loginOfTheOwner;
    private String linkToOwner;
    private String linkToRepo;
    private boolean isFork;

    public int getServerRepoId() {
        return serverRepoId;
    }

    public void setServerRepoId(int serverRepoId) {
        this.serverRepoId = serverRepoId;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLoginOfTheOwner() {
        return loginOfTheOwner;
    }

    public void setLoginOfTheOwner(String loginOfTheOwner) {
        this.loginOfTheOwner = loginOfTheOwner;
    }

    public String getLinkToOwner() {
        return linkToOwner;
    }

    public void setLinkToOwner(String linkToOwner) {
        this.linkToOwner = linkToOwner;
    }

    public String getLinkToRepo() {
        return linkToRepo;
    }

    public void setLinkToRepo(String linkToRepo) {
        this.linkToRepo = linkToRepo;
    }

    public boolean isFork() {
        return isFork;
    }

    public void setFork(boolean fork) {
        isFork = fork;
    }
}
