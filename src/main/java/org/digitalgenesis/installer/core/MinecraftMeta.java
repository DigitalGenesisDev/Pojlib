package org.digitalgenesis.installer.core;

import com.google.gson.annotations.SerializedName;
import org.digitalgenesis.Constants;
import org.digitalgenesis.utils.WebAPIUtils;

public class MinecraftMeta {

    private static final WebAPIUtils handler = new WebAPIUtils(Constants.MOJANG_META_URL);

    public static class MinecraftVersions {
        @SerializedName("versions")
        public MinecraftVersion[] versions;
    }

    public static class MinecraftVersion {
        @SerializedName("id")
        public String id;
        @SerializedName("sha1")
        public String sha1;
    }

    /** @return Array of Minecraft versions. */
    public static MinecraftVersion[] getVersions() {
        return handler.get("mc/game/version_manifest_v2.json", MinecraftVersions.class).versions;
    }

    public static VersionInfo getVersionInfo(String versionName) {
        for (MinecraftVersion minecraftVersion : getVersions()) {
            if (minecraftVersion.id.equals(versionName)) {
                return handler.get(String.format("v1/packages/%s/%s.json", minecraftVersion.sha1, minecraftVersion.id), VersionInfo.class);
            }
        }
        return null;
    }
}