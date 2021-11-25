package pandas;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RarityCalculator {

	public static void main(String[] args) {
		Map<String, Double> traitRarity = new HashMap<String, Double>();
		Map<String, Integer> traitCount = new HashMap<String, Integer>();
		Set<String> traitTypes = new HashSet<String>();

		// collect all trait types
		for (int i = 1; i <= 20000; i++) {
			try (BufferedReader br = new BufferedReader(new FileReader("panda_traits/" + Integer.toString(i) + ".json"))) {
				String line = null;
				StringBuilder sb = new StringBuilder();

				while ((line = br.readLine()) != null) {
					sb.append(line);
				}

				String entireFile = sb.toString();
				JsonParser jsonParser = new JsonParser();
				JsonObject root = (JsonObject) jsonParser.parse(entireFile);

				JsonArray attributes = root.get("attributes").getAsJsonArray();

				for (int j = 0; j < attributes.size(); j++) {
					JsonObject attribute = attributes.get(j).getAsJsonObject();
					String type = attribute.get("trait_type").getAsString();
					traitTypes.add(type);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("processed: " + i);
		}

		traitTypes.remove("MONOLIFF?");
		traitTypes.remove("WEN");
		traitTypes.remove("IDEA_CREDIT");
		traitTypes.remove("B_AND_J");
		traitTypes.remove("TELL_ME_A_JOKE");
		traitTypes.remove("PSYCHOLOGICAL_TRAUMA_LEVEL");
		traitTypes.remove("EXILED");
		traitTypes.remove("RUGGED_COUNT");

		for (String type : traitTypes) {
			System.out.println(type);
		}

		// count how often a trait is in the collection
		for (int i = 1; i <= 20000; i++) {
			try (BufferedReader br = new BufferedReader(
					new FileReader("panda_traits/" + Integer.toString(i) + ".json"))) {
				String line = null;
				StringBuilder sb = new StringBuilder();

				while ((line = br.readLine()) != null) {
					sb.append(line);
				}

				String entireFile = sb.toString();
				JsonParser jsonParser = new JsonParser();
				JsonObject root = (JsonObject) jsonParser.parse(entireFile);

				JsonArray attributes = root.get("attributes").getAsJsonArray();
				List<String> tmpTypes = new ArrayList<String>(traitTypes);

				for (int j = 0; j < attributes.size(); j++) {
					JsonObject attribute = attributes.get(j).getAsJsonObject();
					String type = attribute.get("trait_type").getAsString();
					String value = type + " - " + attribute.get("value").getAsString();

					if (traitTypes.contains(type)) {
						tmpTypes.remove(type);

						Integer count = traitCount.get(value);
						if (count == null)
							count = 0;
						count++;
						traitCount.put(value, count);
					}
				}

				for (String empty : tmpTypes) {
					String emptyType = empty + " - EMPTY";
					Integer count = traitCount.get(emptyType);
					if (count == null)
						count = 0;
					count++;
					traitCount.put(emptyType, count);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("processed: " + i);
		}
		
		Map<String, Integer> sortedTraitCount = sortByValue(traitCount);

		// calculate the rarity of each trait in percentage
		for (Entry<String, Integer> entry : sortedTraitCount.entrySet()) {
			System.out.println(entry.getKey() + " > " + entry.getValue());

			traitRarity.put(entry.getKey(), entry.getValue() / 20000.0);
		}

		// multiply the percentages of each trait to get a rarity
		List<TrashPanda> pandas = new ArrayList<TrashPanda>();

		for (int i = 1; i <= 20000; i++) {
			TrashPanda panda = new TrashPanda();
			panda.number = i;
			pandas.add(panda);

			try (BufferedReader br = new BufferedReader(
					new FileReader("panda_traits/" + Integer.toString(i) + ".json"))) {
				String line = null;
				StringBuilder sb = new StringBuilder();

				while ((line = br.readLine()) != null) {
					sb.append(line);
				}

				String entireFile = sb.toString();
				JsonParser jsonParser = new JsonParser();
				JsonObject root = (JsonObject) jsonParser.parse(entireFile);

				panda.image = root.get("image").getAsString();
				JsonArray attributes = root.get("attributes").getAsJsonArray();
				List<String> tmpTypes = new ArrayList<String>(traitTypes);
				double totalRarity = 1.0;

				for (int j = 0; j < attributes.size(); j++) {
					JsonObject attribute = attributes.get(j).getAsJsonObject();
					String type = attribute.get("trait_type").getAsString();
					String value = type + " - " + attribute.get("value").getAsString();

					if (traitTypes.contains(type)) {
						tmpTypes.remove(type);
						double rarity = traitRarity.get(value);
						totalRarity *= rarity;
					}
				}

				for (String empty : tmpTypes) {
					String emptyType = empty + " - EMPTY";
					double rarity = traitRarity.get(emptyType);
					totalRarity *= rarity;
				}

				panda.rarity = totalRarity;
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("processed: " + i);
		}

		// sort each panda by rarity and print it
		Collections.sort(pandas);

		for (int i = 0; i < pandas.size(); i++) {
			TrashPanda panda = pandas.get(i);
			System.out.println("Rank " + (i + 1) + " > " + panda.number + " / rarity > "
					+ String.format(Locale.US, "%.10f", (panda.rarity * 100.0)) + "% / image > " + panda.image);
		}
	}
	
	public static Map<String, Integer> sortByValue(Map<String, Integer> map) {
		List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
		list.sort(Map.Entry.comparingByValue());
		LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
		list.forEach(e -> sortedMap.put(e.getKey(), e.getValue()));
		return sortedMap;
	}

	static class TrashPanda implements Comparable<TrashPanda> {

		public int number;
		public double rarity;
		public String image;

		@Override
		public int compareTo(TrashPanda o) {
			return Double.compare(rarity, o.rarity);
		}

	}

}
