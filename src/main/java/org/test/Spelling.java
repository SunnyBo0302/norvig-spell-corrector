/**
 * 영어 오타 교정 샘플 프로그램
 * 출처: https://raelcunha.com/spell-correct/
 * */

package org.test;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;

class Spelling {

    private final HashMap<String, Integer> nWords = new HashMap<>();

    /**
     * big.txt에서 단어를 읽어서, 각 단어의 빈도수를 nWords에 넣음
     * */
    public Spelling(String file) throws IOException {
        System.out.println("file = " + Paths.get(file).toAbsolutePath());
        BufferedReader in = new BufferedReader(new FileReader(file));
        Pattern p = Pattern.compile("\\w+");
        for (String temp = ""; temp != null; temp = in.readLine()) {
            Matcher m = p.matcher(temp.toLowerCase());
            while (m.find()) {
                String key = m.group();
                int value = nWords.containsKey(key) ? nWords.get(key) + 1 : 1;
                nWords.put(key, value);
            }
        }
        in.close();
    }

    /**
     * 입력 된 단어에 각 글자를 insertion, deletion, substitution, transposition 해서 list에 넣음
     * 결과의 list는 Damerau-Levenshtein 편집 거리가 1인 단어의 set과 동일하다.
     * */
    private ArrayList<String> edits(String word) {
        ArrayList<String> result = new ArrayList<>();
        // word에서 한글자씩 제거한 것을 result에 넣음 (deletion)
        for (int i = 0; i < word.length(); ++i) {
            result.add(word.substring(0, i) + word.substring(i + 1));
        }

        // word에서 붙어있는 2글자 자리 바꿔 본 것을 result에 넣음 (transposition)
        for (int i = 0; i < word.length() - 1; ++i) {
            result.add(word.substring(0, i) + word.charAt(i + 1) + word.charAt(i) + word.substring(i + 2));
        }

        // word에 포함 된 글자에 한 글자씩 대치 한 것을 result에 넣음 (substitution)
        for (int i = 0; i < word.length(); ++i) {
            for (char c = 'a'; c <= 'z'; ++c) {
                result.add(word.substring(0, i) + c + word.substring(i + 1));
            }
        }

        // word에 한 글자씩 넣은 것을 result에 넣음 (insertion)
        for (int i = 0; i <= word.length(); ++i) {
            for (char c = 'a'; c <= 'z'; ++c) {
                result.add(word.substring(0, i) + c + word.substring(i));
            }
        }

        return result;
    }

    /**
     * 단어를 요리조리 변환시킨 list에서 big.txt에서 등장한 단어가 있으면 그 녀석을 return
     * */
    public final String correct(String word) {
        // nWords에 있으면 바로 리턴
        if (nWords.containsKey(word)) {
            return word;
        }

        //list에는 word에 단어 추가, 삭제, 대치, transpose한 단어들이 들어가 있다
        ArrayList<String> list = edits(word);
        HashMap<Integer, String> candidates = new HashMap<>();
        for (String s : list) {
            //nWords에 포함 된 단어면 candidate에 빈도수를 key로 넣음
            if (nWords.containsKey(s)) {
                candidates.put(nWords.get(s), s);
            }
        }

        //빈도수가 가장 높은 친구를 return
        if (candidates.size() > 0) {
            return candidates.get(Collections.max(candidates.keySet()));
        }

        //아무런 candidate도 없다! 그렇다면 list에서 한 문장씩 해서 candidate를 찾음
        for (String s : list) {
            for (String w : edits(s)) {
                if (nWords.containsKey(w)) {
                    candidates.put(nWords.get(w), w);
                }
            }
        }

        // 가장 빈도수가 높은 단어. 아예 없다면 단어 그대로 return
        return candidates.size() > 0 ? candidates.get(Collections.max(candidates.keySet())) : word;
    }

    public static void main(String[] args) throws IOException {
        Spelling spelling = new Spelling("src/main/resources/big.txt");
        Scanner scanner = new Scanner(System.in);

        String line;
        while(!(line = scanner.nextLine()).equals("exit"))
        {
            System.out.println("correction = " + spelling.correct(line));
        }
    }
}
