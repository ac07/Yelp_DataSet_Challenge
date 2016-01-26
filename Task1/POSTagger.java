package Task1;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by jay on 11/28/15.
 */
public class POSTagger {

    public MaxentTagger tagger;

    public POSTagger(MaxentTagger tagger) {
        this.tagger = tagger;
    }

    public String tag(String str, ArrayList<String> tags) throws IOException, ClassNotFoundException {

        String tagged = tagger.tagString(str);

        StringBuilder stringBuilder = new StringBuilder();

        String[] split = tagged.split(" ");

        for (String s : split) {
            int lastIndex = s.lastIndexOf("_");
            if (tags.contains(s.substring(lastIndex + 1))) {
                String substr = s.substring(0, lastIndex);
                stringBuilder.append(substr + " ");
            }
        }

        return stringBuilder.toString();

    }

}
