package org.digitalgenesis.account;

import org.digitalgenesis.API;
import org.digitalgenesis.utils.GsonUtils;

import java.io.File;

public class MinecraftAccount {
    public String accessToken;
    public String uuid;
    public String username;
    public boolean isDemoMode = false;
    public long expiresOn;
    public final String userType = "msa";

    public static MinecraftAccount login(String gameDir, String msToken) throws MSAException {
        MsaAuth instance = new MsaAuth();
        MinecraftAccount account = instance.performLogin(msToken);

        GsonUtils.objectToJsonFile(gameDir + "/" + account.uuid + ".json", account);
        return account;
    }

    public static boolean removeAccount(String uuid) {
        File accountFile = new File(API.accountFilesDir + uuid + ".json");
        File accountCache = new File(API.cacheDirectory);

        return accountFile.delete() && accountCache.delete();
    }

    //Try this before using login - the account will have been saved to disk if previously logged in
    public static MinecraftAccount load(String path, String uuid) {
        return GsonUtils.jsonFileToObject(path + "/" + uuid + ".json", MinecraftAccount.class);
    }
}
