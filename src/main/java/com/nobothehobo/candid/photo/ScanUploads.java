package com.nobothehobo.candid.photo;

import com.nobothehobo.candid.network.ScanChunkPayload;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

/** Server-thread only; one 64 KiB pending image per player, expires after ten seconds. */
public final class ScanUploads {
    private record Upload(UUID shot,byte[] pixels,int next,long expires){}
    private static final Map<UUID,Upload> pending=new HashMap<>();
    private ScanUploads(){}
    public static void accept(ServerPlayer player,ScanChunkPayload chunk){
        if(chunk.colors().length!=16384||chunk.index()<0||chunk.index()>3)return;
        UUID id;try{id=UUID.fromString(chunk.shot());}catch(IllegalArgumentException e){return;}
        long now=System.currentTimeMillis();pending.values().removeIf(u->u.expires<now);
        if(chunk.index()==0)pending.put(player.getUUID(),new Upload(id,new byte[65536],0,now+10000));
        Upload u=pending.get(player.getUUID());if(u==null||!u.shot.equals(id)||u.next!=chunk.index())return;
        for(byte b:chunk.colors())if((b&255)<4||(b&255)>247){pending.remove(player.getUUID());return;}
        System.arraycopy(chunk.colors(),0,u.pixels,chunk.index()*16384,16384);
        pending.put(player.getUUID(),new Upload(id,u.pixels,u.next+1,u.expires));
    }
    public static String take(ServerPlayer player,String shot){
        Upload u=pending.remove(player.getUUID());return u!=null&&u.next==4&&u.shot.toString().equals(shot)&&u.expires>=System.currentTimeMillis()?Base64.getEncoder().encodeToString(u.pixels):null;
    }
    public static void clear(UUID player){pending.remove(player);}
    public static void clear(){pending.clear();}
}
