package com.nobothehobo.candid.core;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

/** Local, explicit sharing export. No upload, credentials or external service is involved. */
public final class PhotoExport {
    private PhotoExport(){}
    public static Path write(Path directory,String filename,byte[] png)throws IOException{
        if(filename==null||!filename.endsWith(".png"))throw new IllegalArgumentException("Invalid export filename");
        UUID id=UUID.fromString(filename.substring(0,filename.length()-4));
        if(png.length<24||png.length>524288||java.nio.ByteBuffer.wrap(png).getLong()!=0x89504e470d0a1a0aL)throw new IllegalArgumentException("Invalid PNG export");
        Files.createDirectories(directory);Path target=directory.resolve(id+".png"),temp=Files.createTempFile(directory,"candid-",".tmp");
        try{Files.write(temp,png);try{Files.move(temp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);}}
        finally{Files.deleteIfExists(temp);}return target;
    }
}
