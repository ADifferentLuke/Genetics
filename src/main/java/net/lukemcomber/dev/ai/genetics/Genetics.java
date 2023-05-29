package net.lukemcomber.dev.ai.genetics;

import net.lukemcomber.dev.ai.genetics.service.GenomeStreamReader;
import net.lukemcomber.dev.ai.genetics.service.TerrainStreamReader;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Genetics {

    private final String initFile;
    private final String zooFile;
    public Genetics( final String initFile, final String zooFile ){
       this.initFile = initFile;
       this.zooFile = zooFile;
    }

    public void run() throws IOException {
        final Terrain terrain = new TerrainStreamReader().parse(Files.newInputStream(Paths.get(initFile)));
        final GenomeStreamReader genomeStreamReader = new GenomeStreamReader(terrain.getSizeOfXAxis(),
                terrain.getSizeOfYAxis(), terrain.getSizeOfZAxis());
        if(StringUtils.isNotEmpty(zooFile)) {
            final List<GenomeStreamReader.OrganismLocation> organisms = genomeStreamReader.parse(Files.newInputStream(Paths.get(zooFile)));
            for( int i = 0; organisms.size() > i; ++i ){
                //terrain.setCell( )
            }
        }
    }

    public static void main(final String[] args) throws IOException {
        if( 1 != args.length ){
            System.err.println( "Usage: Genetics <initialization terrain file> [<initialize biology file>]");
            System.exit(-1);
        }
        new Genetics(args[0],args[1]).run();
    }
}
