package org.ansj.library;

import static org.ansj.util.MyStaticValue.LIBRARYLOG;

import java.io.*;

import org.ansj.dic.DicReader;
import org.ansj.util.MyStaticValue;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.domain.SmartForest;
import org.nlpcn.commons.lang.tire.domain.Value;
import org.nlpcn.commons.lang.tire.library.Library;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.StringUtil;

/**
 * 用户自定义词典操作类
 * 
 * @author ansj
 */
public class UserDefineLibrary {

	public static final String DEFAULT_NATURE = "userDefine";

	public static final Integer DEFAULT_FREQ = 1000;

	public static final String DEFAULT_FREQ_STR = "1000";

	public static Forest FOREST = null;

	public static Forest ambiguityForest = null;

	static {
		initUserLibrary();
		initAmbiguityLibrary();
	}

	/**
	 * 关键词增加
	 * 
	 * @param keyword
	 *            所要增加的关键词
	 * @param nature
	 *            关键词的词性
	 * @param freq
	 *            关键词的词频
	 */
	public static void insertWord(String keyword, String nature, int freq) {
		String[] paramers = new String[2];
		paramers[0] = nature;
		paramers[1] = String.valueOf(freq);
		Value value = new Value(keyword, paramers);
		Library.insertWord(FOREST, value);
	}

	/**
	 * 加载纠正词典
	 */
	private static void initAmbiguityLibrary() {
		String ambiguityLibrary = MyStaticValue.ambiguityLibrary;
		if (StringUtil.isBlank(ambiguityLibrary)) {
			LIBRARYLOG.warn(
					"init ambiguity  warning :" + ambiguityLibrary + " because : file not found or failed to read !");
			return;
		}
//		ambiguityLibrary = MyStaticValue.ambiguityLibrary;
		InputStream ambiguityInputStream = DicReader.getInputStream(ambiguityLibrary);
		if (ambiguityInputStream != null) {

			try {
				ambiguityForest = Library.makeForest(ambiguityInputStream);
			} catch (Exception e) {

				LIBRARYLOG.error("error make ambiguity forest ! error message is " + e.getMessage());
			}
			LIBRARYLOG.info("init ambiguityLibrary ok!");
		} else {
			LIBRARYLOG.warn("init ambiguity  warning :" + new File(ambiguityLibrary).getAbsolutePath()
					+ " because : file not found or failed to read !");
		}
	}

	/**
	 * 加载用户自定义词典和补充词典
	 */
	private static void initUserLibrary() {

		try {
			FOREST = new Forest();
			// 加载用户自定义词典
			String userLibraryStr = MyStaticValue.userLibrary;
			String[] userLibraries = userLibraryStr.split(",");
			for (String userLibrary : userLibraries) {

				loadLibrary(FOREST, userLibrary);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 单个文件加载词典
	public static void loadFile(Forest forest, BufferedReader br) {
		if (br == null) {
			LIBRARYLOG.warn("file in path can not to read!");
			return;
		}
		String temp = null;
		LIBRARYLOG.info("buffer is " + br);
		String[] strs = null;
		Value value = null;
		try {
//			br = IOUtil.getReader(new FileInputStream(file), "UTF-8");
			while ((temp = br.readLine()) != null) {
				if (StringUtil.isBlank(temp)) {
					continue;
				} else {
					strs = temp.split("\t");

					strs[0] = strs[0].toLowerCase();

					// 如何核心辞典存在那么就放弃
					if (MyStaticValue.isSkipUserDefine && DATDictionary.getId(strs[0]) > 0) {
						continue;
					}

					if (strs.length != 3) {
						value = new Value(strs[0], DEFAULT_NATURE, DEFAULT_FREQ_STR);
					} else {
						value = new Value(strs[0], strs[1], strs[2]);
					}
					Library.insertWord(forest, value);
				}
			}
			LIBRARYLOG.info("init user userLibrary ok!!");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtil.close(br);
			br = null;
		}
	}

	/**
	 * 加载词典,传入一本词典的路径.或者目录.词典后缀必须为.dic
	 */
	public static void loadLibrary(Forest forest, String path) {
		// 加载用户自定义词典
		LIBRARYLOG.info("input path is " + path);
		String rootDir = UserDefineLibrary.class.getResource("/").getPath();
		LIBRARYLOG.info("root dir is " + rootDir + ", path is " + path);
		loadFile(forest, DicReader.getReader(path));
//		LIBRARYLOG.info("path abs is " + DicReader.getInputStream(path));
//		LIBRARYLOG.info("root dir is " + rootDir);
//		File file = new File(rootDir + path);
//		if (path != null) {
//
//			if (file.isFile()) {
//				loadFile(forest, DicReader.getReader(path));
//			} else if (file.isDirectory()) {
//				File[] files = file.listFiles();
//				if (files != null) {
//					for (File file1 : files) {
//						String fileName = file1.getName().trim();
//						if (fileName.endsWith(".dic")) {
//							LIBRARYLOG.info("---------- file path is " + fileName);
//							loadFile(forest, DicReader.getReader(path + fileName));
//						}
//					}
//				}
//			} else {
//				LIBRARYLOG.warn("init user library  error :" + new File(path).getAbsolutePath()
//						+ " because : not find that file !");
//			}
//		}
	}

	/**
	 * 删除关键词
	 */
	public static void removeWord(String word) {
		Library.removeWord(FOREST, word);
	}

	public static String[] getParams(String word) {
		return getParams(FOREST, word);
	}

	public static String[] getParams(Forest forest, String word) {
		SmartForest<String[]> temp = forest;
		for (int i = 0; i < word.length(); i++) {
			temp = temp.get(word.charAt(i));
			if (temp == null) {
				return null;
			}
		}
		if (temp.getStatus() > 1) {
			return temp.getParam();
		} else {
			return null;
		}
	}

	public static boolean contains(String word) {
		return getParams(word) != null;
	}

	/**
	 * 将用户自定义词典清空
	 */
	public static void clear() {
		FOREST.clear();
	}

}
