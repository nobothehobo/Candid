package com.nobothehobo.candid.photo;

import com.google.gson.Gson;
import com.nobothehobo.candid.core.RollState;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/** Server-thread records, serialized in order by one background writer. One file per roll. */
public final class RollRepository implements AutoCloseable {
    private final Map<UUID,RollState> records=new HashMap<>();
    private final Set<UUID> dirty=new HashSet<>();
    private final Path directory;
    private final Gson gson=new Gson();
    private final ExecutorService io=Executors.newSingleThreadExecutor(r->new Thread(r,"Candid-roll-storage"));
    private final AtomicReference<Throwable> error=new AtomicReference<>();
    public RollRepository(Path directory)throws Exception {
        this.directory=directory;Files.createDirectories(directory);
        try(var files=Files.list(directory)){
            for(Path p:files.filter(p->p.toString().endsWith(".json")).toList()){
                var r=gson.fromJson(Files.readString(p),RollState.class);
                if(r==null||!p.getFileName().toString().equals(r.id()+".json")||records.put(r.id(),r)!=null)throw new IllegalArgumentException("Invalid roll save: "+p.getFileName());
            }
        }catch(Exception e){io.shutdown();throw e;}
    }
    public void saveScan(UUID id,byte[] png){
        healthy();byte[] copy=png.clone();io.execute(()->{
            try{Path scans=directory.resolveSibling("scans");Files.createDirectories(scans);Path temp=scans.resolve(id+".tmp"),target=scans.resolve(id+".png");
                Files.write(temp,copy);try{Files.move(temp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);}
            }catch(Exception e){error.compareAndSet(null,e);}
        });
    }
    public CompletableFuture<byte[]> readScan(UUID id){
        return CompletableFuture.supplyAsync(()->{try{Path file=directory.resolveSibling("scans").resolve(id+".png");return Files.isRegularFile(file)&&Files.size(file)<=524288?Files.readAllBytes(file):new byte[0];}catch(java.io.IOException e){return new byte[0];}},io);
    }
    public RollState get(UUID id){healthy();var r=records.get(id);if(r==null)throw new IllegalStateException("No valid negative record for this roll");return r;}
    public void put(RollState r){healthy();records.put(r.id(),r);dirty.add(r.id());}
    private void healthy(){if(error.get()!=null)throw new IllegalStateException("Photo storage failed; preserve your save and check the game log",error.get());}
    public void flush(){
        healthy();for(UUID id:dirty){RollState record=records.get(id);io.execute(()->{
            try {Path file=directory.resolve(id+".json"),temp=directory.resolve(id+".tmp");Files.writeString(temp,gson.toJson(record));
                if(Files.exists(file))Files.copy(file,directory.resolve(id+".bak"),StandardCopyOption.REPLACE_EXISTING);
                try{Files.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);}
            }catch(Exception e){error.compareAndSet(null,e);}
        });}dirty.clear();
    }
    public void close()throws Exception{try{flush();}finally{io.shutdown();}if(!io.awaitTermination(60,TimeUnit.SECONDS))throw new IllegalStateException("Photo save did not finish");healthy();}
}
