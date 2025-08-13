package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
//追加パッケージ
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)		

		
		File[] files = new File(args[0]).listFiles();
		
		List<File> rcdFiles =new ArrayList<>();
		//ファイルの数繰り返し処理
		for (int i = 0;i < files.length ; i++) {
			//files[i].getnName();//ファイル名取得
			if(files[i].getName().matches("[0-9]{8}.rcd")){
				rcdFiles.add(files[i]);
				
			}
		}
	
		
		// 売上ファイル読み込み処理

		for (int i = 0;i< rcdFiles.size(); i++) {
			BufferedReader br = null;
	
			try {
				File file = new File(args[0], files[i].getName());
				
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);
					String line;
				//売上ファイル支店コード、売れ上げ金額のリスト
				List<String> items = new ArrayList<>();
				// 一行ずつ読みこむ
				while((line = br.readLine()) != null) {
					items.add(line);
					
				}
				
				long fileSale = Long.parseLong(items.get(1));
				//読み込んだ売上金額を加算
					
				Long saleAmount = branchSales.get(items.get(0)) + fileSale;
				//mapに加算した値を追加
				branchSales.put( items.get(0),saleAmount);
				
	
			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return ;//終了させるには
			} finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}
		//3-1支店別集計ファイル書き込み
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT , branchNames, branchSales)) {
			return;
		}
	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			//File file = new File("C:\\Users\\trainee1206\\Desktop\\売上集計課題\\branch.lst","branch.lst");
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] items = line.split(",");

				//Mapに追加する２つの情報をputの引数として指定
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L );
				System.out.println(line);
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;

		try {
			File file = new File(path, fileName);
			
			//ファイルライターの変数fw
			FileWriter fw = new FileWriter(file);

			//バッファードライターの変数bw
			bw = new BufferedWriter(fw);
			


			
			// 一行ずつ書き込む支店コード、支店名、売上金額
			for(String key : branchNames.keySet()) {
				bw.write(key +","+branchNames.get(key)+","+branchSales.get(key));
				bw.newLine(); 
				

			}


		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
		}

	}