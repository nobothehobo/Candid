package com.nobothehobo.candid.core;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AssetTest {
    @Test void shapedRecipesDoNotCollideWhenMirrored() throws Exception {
        Map<String,String> seen=new HashMap<>();
        try(var files=Files.list(Path.of("src/main/resources/data/candid/recipe"))){
            for(var file:files.filter(p->p.toString().endsWith(".json")).sorted().toList()){
                var recipe=JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                if(!recipe.has("pattern"))continue;
                List<String> rows=new ArrayList<>();
                for(var row:recipe.getAsJsonArray("pattern"))rows.add(row.getAsString());
                while(rows.getFirst().isBlank())rows.removeFirst();
                while(rows.getLast().isBlank())rows.removeLast();
                int left=rows.getFirst().length(),right=0;
                for(var row:rows)for(int x=0;x<row.length();x++)if(row.charAt(x)!=' '){left=Math.min(left,x);right=Math.max(right,x);}
                StringBuilder normal=new StringBuilder(),mirror=new StringBuilder();
                for(var row:rows){
                    for(int x=left;x<=right;x++){
                        char a=row.charAt(x),b=row.charAt(right+left-x);
                        normal.append(a==' '?"-":recipe.getAsJsonObject("key").get(""+a).getAsString()).append(',');
                        mirror.append(b==' '?"-":recipe.getAsJsonObject("key").get(""+b).getAsString()).append(',');
                    }
                    normal.append(';');mirror.append(';');
                }
                String key=normal.toString().compareTo(mirror.toString())<0?normal.toString():mirror.toString();
                assertNull(seen.putIfAbsent(key,file.getFileName().toString()),"Mirrored crafting collision: "+file);
            }
        }
    }
    @Test void exportedAtlasUvsAreNormalized() throws Exception {
        for(String model : new String[]{"item/camera", "block/darkroom_basin", "block/enlarger", "block/tripod", "item/lens_28", "item/lens_35", "item/lens_50", "item/lens_90", "item/camera_28", "item/camera_35", "item/camera_50", "item/camera_90", "item/remote_release"}) {
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
