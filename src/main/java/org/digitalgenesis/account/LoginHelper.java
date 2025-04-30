package org.digitalgenesis.account;

import com.microsoft.aad.msal4j.DeviceCode;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SilentParameters;
import org.digitalgenesis.API;
import org.digitalgenesis.utils.GsonUtils;
import org.digitalgenesis.utils.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class LoginHelper {
    public static final Set<String> SCOPES;
    private static Thread loginThread;
    private static PublicClientApplication pca;

    static {
        try {
            File cache = new File(API.cacheDirectory + "/serialized_cache.json");
            if(!cache.exists()) {
                cache.getParentFile().mkdirs();
                cache.createNewFile();
            }

            // Loads cache from file
            BufferedReader reader = new BufferedReader(new FileReader(cache.getPath()));
            String dataToInitCache = reader.readLine();
            reader.close();

            TokenPersistence persistenceAspect = new TokenPersistence(dataToInitCache, cache);

            pca = PublicClientApplication.builder(API.msaClientID)
                    .setTokenCacheAccessAspect(persistenceAspect)
                    .authority("https://login.microsoftonline.com/consumers/")
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SCOPES = new HashSet<>();
        SCOPES.add("XboxLive.SignIn");
        SCOPES.add("XboxLive.offline_access");
    }

    public static MinecraftAccount refreshAccount(String uuid) {
        Set<IAccount> accountsInCache = pca.getAccounts().join();
        IAuthenticationResult result;
        try {
            for (IAccount account : accountsInCache) {
                SilentParameters silentParameters =
                        SilentParameters
                                .builder(SCOPES, account)
                                .build();

                result = pca.acquireTokenSilently(silentParameters).join();
                MinecraftAccount acc = new MsaAuth().performLogin(result.accessToken());
                GsonUtils.objectToJsonFile(API.accountFilesDir + acc.uuid + ".json", acc);
                if (!acc.uuid.equals(uuid)) {
                    // Refresh was for the wrong acc, try again
                    continue;
                }
                return acc;
            }
            return null;
        } catch (Exception ex) {
            Logger.getInstance().appendToLog("Couldn't refresh token! " + ex);
            return null;
        }
    }

    public static void login() {
        loginThread = new Thread(() -> {
            Consumer<DeviceCode> deviceCodeConsumer = (DeviceCode deviceCode) -> API.msaMessage = deviceCode.message();
            CompletableFuture<IAuthenticationResult> future = pca.acquireToken(
                    DeviceCodeFlowParameters.builder(SCOPES, deviceCodeConsumer).build());

            try {
                IAuthenticationResult res = future.get();
                while(res.account() == null) {
                    Thread.sleep(20);
                }
                try {
                    API.currentAcc = MinecraftAccount.login(API.accountFilesDir, res.accessToken());
                } catch (Exception e) {
                    Logger.getInstance().appendToLog("Unable to load account! | " + e);
                }
                API.profileName = API.currentAcc.username;
                API.profileUUID = API.currentAcc.uuid;
            } catch (ExecutionException | InterruptedException e) {
                Logger.getInstance().appendToLog("MicrosoftLogin | Something went wrong! Couldn't reach the Microsoft Auth servers.");
                API.msaReturnMessage = "MicrosoftLogin | Something went wrong! Couldn't reach the Microsoft Auth servers.";
            }
        });

        loginThread.start();
    }
}
