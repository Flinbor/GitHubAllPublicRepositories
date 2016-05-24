
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

package in.flinbor.github.publicRepositories.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Data Transfer Object for github Permissions
 */
public class PermissionsDTO {

    @SerializedName("admin")
    @Expose
    private boolean admin;
    @SerializedName("push")
    @Expose
    private boolean push;
    @SerializedName("pull")
    @Expose
    private boolean pull;

    /**
     * @return The admin
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * @param admin The admin
     */
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    /**
     * @return The push
     */
    public boolean isPush() {
        return push;
    }

    /**
     * @param push The push
     */
    public void setPush(boolean push) {
        this.push = push;
    }

    /**
     * @return The pull
     */
    public boolean isPull() {
        return pull;
    }

    /**
     * @param pull The pull
     */
    public void setPull(boolean pull) {
        this.pull = pull;
    }

}
