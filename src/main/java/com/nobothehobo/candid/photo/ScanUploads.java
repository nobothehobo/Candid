package com.nobothehobo.candid.photo;

import com.nobothehobo.candid.network.ScanChunkPayload;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

/** Server-thread only; one bounded pending image per player, expires after ten seconds. */
public final class ScanUploads {
    private static final class Upload {
        final UUID shot;final byte[] pixels=new byte[65536];final long expires=System.currentTimeMillis()+10000;
        byte[] scan;int paletteNext,scanNext;
        Upload(UUID shot){this.shot=shot;}
    }
    public record Result(String colors,byte[] png){}
    private static final Map<UUID,Upload> pending=new HashMap<>();
    private ScanUploads(){}
    public static void accept(ServerPlayer player,ScanChunkPayload chunk){
        if(chunk.colors().length<1||chunk.colors().length>16384||chunk.index()<0||chunk.index()>31||chunk.kind()<0||chunk.kind()>1)return;
        if(chunk.total()<1||chunk.total()>524288)return;
        if(chunk.kind()==0&&(chunk.total()!=65536||chunk.index()>3||chunk.colors().length!=16384))return;
        UUID id;try{id=UUID.fromString(chunk.shot());}catch(IllegalArgumentException e){return;}
        long now=System.currentTimeMillis();pending.values().removeIf(u->u.expires<now);
        if(chunk.kind()==0&&chunk.index()==0)pending.put(player.getUUID(),new Upload(id));
        Upload u=pending.get(player.getUUID());if(u==null||!u.shot.equals(id))return;
        if(chunk.kind()==0){
            if(u.paletteNext!=chunk.index())return;
            for(byte b:chunk.colors())if((b&255)<4||(b&255)>247){pending.remove(player.getUUID());return;}
            System.arraycopy(chunk.colors(),0,u.pixels,chunk.index()*16384,16384);u.paletteNext++;
        }else{
            if(u.paletteNext!=4||u.scanNext!=chunk.index())return;
            if(chunk.index()==0)u.scan=new byte[chunk.total()];
            if(u.scan==null||u.scan.length!=chunk.total())return;
            int offset=chunk.index()*16384;if(offset>=u.scan.length||chunk.colors().length!=Math.min(16384,u.scan.length-offset))return;
            System.arraycopy(chunk.colors(),0,u.scan,offset,chunk.colors().length);u.scanNext++;
        }
    }
    public static Result take(ServerPlayer player,String shot){
        Upload u=pending.remove(player.getUUID());
        if(u==null||u.paletteNext!=4||!u.shot.toString().equals(shot)||u.expires<System.currentTimeMillis())return new Result(null,new byte[0]);
        byte[] png=u.scan;
        // Fixed-size raster only; never accept unbounded image headers for later client decode.
        if(png==null||u.scanNext!=(png.length+16383)/16384||png.length<24||java.nio.ByteBuffer.wrap(png).getLong()!=0x89504e470d0a1a0aL||java.nio.ByteBuffer.wrap(png,16,8).getInt()!=504||java.nio.ByteBuffer.wrap(png,20,4).getInt()!=336)png=new byte[0];
        return new Result(Base64.getEncoder().encodeToString(u.pixels),png);
    }
    public static void clear(UUID player){pending.remove(player);}
    public static void clear(){pending.clear();}
}
