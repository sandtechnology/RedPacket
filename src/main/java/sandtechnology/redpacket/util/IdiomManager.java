package sandtechnology.redpacket.util;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.logging.Level;

import static sandtechnology.redpacket.RedPacketPlugin.getInstance;
import static sandtechnology.redpacket.RedPacketPlugin.log;

//copy and merge form JieLong/src/main/java/top/seraphjack/jielong/idiom
//https://github.com/SeraphJACK/JieLong

/**
 * @author SeraphJACK
 */

public class IdiomManager {

    private static final Map<String, POJOIdiom> idiomMap = new HashMap<>();
    private static final List<String> idiomList = new ArrayList<>();

    private IdiomManager() {
    }

    public static void debugSetup(Object ref) {
        InputStream fis = ref.getClass().getClassLoader().getResourceAsStream("idiom.json");
        if (fis == null) {
            throw new RuntimeException("idiom.json丢失！将无法进行成语接龙！");
        }
        Gson gson = new Gson();
        POJOIdiom[] idioms = gson.fromJson(new InputStreamReader(fis, StandardCharsets.UTF_8), POJOIdiom[].class);
        Arrays.stream(idioms).forEach(i -> idiomMap.put(i.word, i));
        idiomList.addAll(idiomMap.keySet());
    }

    public static void setup() {
        long startTime = System.currentTimeMillis();
        log(Level.INFO, "从Jar中加载成语数据库(idiom.json)...");
        InputStream fis = getInstance().getClass().getClassLoader().getResourceAsStream("idiom.json");
        if (fis == null) {
            log(Level.SEVERE, "idiom.json丢失！将无法进行成语接龙！");
            throw new RuntimeException("idiom.json丢失！将无法进行成语接龙！");
        }
        Gson gson = new Gson();
        POJOIdiom[] idioms = gson.fromJson(new InputStreamReader(fis, StandardCharsets.UTF_8), POJOIdiom[].class);
        Arrays.stream(idioms).forEach(i -> idiomMap.put(i.word, i));
        idiomList.addAll(idiomMap.keySet());
        log(Level.INFO, "成语数据库载入完成，花费了" + (System.currentTimeMillis() - startTime) + "毫秒。");
    }

    public static void reload() {
        idiomMap.clear();
        idiomList.clear();
        setup();
    }

    public static boolean isValidIdiom(String idiom) {
        return idiomMap.containsKey(idiom);
    }

    public synchronized static String getRandomIdiom(){
        Collections.shuffle(idiomList);
        return idiomList.get(0);
    }

    public static boolean isValidSequence(String former, String idiom) {
        if (!(isValidIdiom(former) && isValidIdiom(idiom))) {
            return false;
        } else {
            return Objects.equals(getLastPinyin(idiomMap.get(former).pinyin), getFirstPinyin(idiomMap.get(idiom).pinyin));
        }
    }

    private static String removeRedundantCharacters(String in) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < in.length(); i++) {
            //                    | Magic value
            if (in.charAt(i) <= 500) {
                sb.append(in.charAt(i));
            }
        }
        return sb.toString();
    }

    private static String getLastPinyin(String pinyin) {
        for (int i = pinyin.length() - 1; i > 0; i--) {
            if (pinyin.charAt(i) == ' ') {
                return removeRedundantCharacters(Normalizer.normalize(pinyin.substring(i + 1), Normalizer.Form.NFKD));
            }
        }
        return null;
    }

    private static String getFirstPinyin(String pinyin) {
        for (int i = 0; i < pinyin.length(); i++) {
            if (pinyin.charAt(i) == ' ') {
                return removeRedundantCharacters(Normalizer.normalize(pinyin.substring(0, i), Normalizer.Form.NFKD));
            }
        }
        return null;
    }
    public static String getIdiomPinyin(String idiom){
        return idiomMap.containsKey(idiom)? getLastPinyin(idiomMap.get(idiom).pinyin):"无";
    }

    private static class POJOIdiom {
        String derivation, example, explanation, pinyin, abbreviation, word;
    }

}
