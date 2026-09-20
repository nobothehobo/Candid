package com.nobothehobo.candid.core;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AssetTest {
    @Test void exportedAtlasUvsAreNormalized() throws Exception {
        for(String model : new String[]{"item/camera", "block/darkroom_basin"}) {
            try(var input=getClass().getResourceAsStream("/assets/candid/models/"+model+".json")) {
                assertNotNull(input);
                var root=JsonParser.parseReader(new InputStreamReader(input,StandardCharsets.UTF_8)).getAsJsonObject();
                for(var element:root.getAsJsonArray("elements"))
                    for(var face:element.getAsJsonObject().getAsJsonObject("faces").entrySet())
                        for(var coordinate:face.getValue().getAsJsonObject().getAsJsonArray("uv"))
                            assertTrue(coordinate.getAsDouble()>=0&&coordinate.getAsDouble()<=16, model+" has pixel-space UVs");
            }
        }
    }
}
