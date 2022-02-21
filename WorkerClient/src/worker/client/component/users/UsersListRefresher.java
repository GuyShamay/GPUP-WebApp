package worker.client.component.users;
import com.google.gson.*;
import dto.users.UserDTO;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import worker.client.util.Constants;
import worker.client.util.HttpClientUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class UsersListRefresher extends TimerTask {

    private final Consumer<List<UserDTO>> usersListConsumer;
    private final Consumer<String> errorConsumer;
    private final BooleanProperty shouldUpdate;


    public UsersListRefresher(BooleanProperty shouldUpdate, Consumer<List<UserDTO>> usersListConsumer, Consumer<String> errorConsumer) {
        this.shouldUpdate = shouldUpdate;
        this.usersListConsumer = usersListConsumer;
        this.errorConsumer = errorConsumer;
    }

    @Override
    public void run() {
        if (!shouldUpdate.get()) {
            return;
        }

        HttpClientUtil.runAsync(Constants.USERS_LIST, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> errorConsumer.accept("Error: failed request"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String usersListAsJsonString = response.body().string();
                JsonArray jsonArray = JsonParser.parseString(usersListAsJsonString).getAsJsonArray();
                usersListConsumer.accept(parseUserslist(jsonArray));
            }
        });
    }

    private List<UserDTO> parseUserslist(JsonArray jsonArray) {
        List<UserDTO> list = null;
        if (jsonArray != null) {
            list = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                UserDTO user = new UserDTO(jsonObject.get("username").getAsString(), jsonObject.get("role").getAsString());
                list.add(user);
            }
        }
        return list;
    }
}

