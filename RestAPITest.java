import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

public class RestAPITest {
    public static void main(String[] args){
        String currencySearch = "gbp";

        HashMap<String, Country> countryMap = new HashMap<>();  //<name, details>

        //get list of countries WITH the currency
        String res = getResponse("https://restcountries.eu/rest/v2/currency/", currencySearch);

        JSONArray currencyList, borderList, countriesWithCurrency = new JSONArray(res);
        JSONObject country;

        ArrayList<String> currencies = new ArrayList<>();   //list of currency codes that each country will have

        //for each JSON country that is returned by the API request
        for (int i=0; i < countriesWithCurrency.length(); i++) {
            country = countriesWithCurrency.getJSONObject(i);

            //get the list of currencies this country uses then add to a list
            currencyList = country.getJSONArray("currencies");
            for (int j=0; j < currencyList.length(); j++) {
                currencies.add(currencyList.getJSONObject(j).getString("code"));
            }

            //create new country object and add to the result map
            Country c = new Country(
                    country.getString("name"),
                    country.getString("alpha3Code"),
                    currencies,
                    country.getString("region")
            );
            countryMap.put(country.getString("name"), c);

            currencies.clear();
        }

        //get all the matching names that we need to answer Q1
        List<String> names = countryMap.values().stream()
                .map(Country::getName)
                .collect(Collectors.toList());
        System.out.println("--- Question 1: Countries with currency " + currencySearch + "---\n" + names.toString() + "\n\n");

        //QUESTION 2
        HashMap<String, Country> neighboursWithDifferentCurrency = new HashMap<>();     //record countries that border, but have different currency to those in Q1

        //iterate over all countries found in Q1
        for (Map.Entry<String, Country> c : countryMap.entrySet()) {
            res = getResponse("https://restcountries.eu/rest/v2/region/", c.getValue().getRegion());

            //loop over all countries that border 'c'
            borderList = new JSONArray(res);
            for (int i=0; i<borderList.length(); i++) {
                country = borderList.getJSONObject(i);
                JSONArray currencyListBorder = country.getJSONArray("currencies");

                for (int j=0; j < currencyListBorder.length(); j++) {
                    try {
                        currencies.add(currencyListBorder.getJSONObject(j).getString("code"));
                    } catch(Exception e) { }    //WTF null currency code thanks virgin islands
                }

                //if it doesn't use the original currency we're searching, and it doesn't already exist in our hashmap, add it
                if (!currencies.contains(currencySearch) && (neighboursWithDifferentCurrency.get(country.getString("name")) == null)) {
                    neighboursWithDifferentCurrency.put(country.getString("name"), new Country(
                            country.getString("name"),
                            country.getString("alpha3Code"),
                            currencies,
                            country.getString("region")));
                }
                currencies.clear();
            }
        }

        List<String> namesBordering = neighboursWithDifferentCurrency.values().stream()
                .map(Country::getName)
                .collect(Collectors.toList());

        System.out.println("--- Question 2: Countries that border the countries in Q1, but have different currencies ---\n" + namesBordering.toString());
    }

    //accepts a url and a paramter to make a get request of the API
    public static String getResponse(String url, String param) {
        StringBuilder response = new StringBuilder();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url + param).openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == 200) {
                Scanner scanIn = new Scanner(connection.getInputStream());
                while(scanIn.hasNextLine()) {
                    response.append(scanIn.nextLine()).append("\n");
                }
                scanIn.close();
            }
        } catch(Exception e ) {}

        if (response.toString().equals("")) {
            return null;
        } else {
            return response.toString();
        }
    }
}

class Country {
    private String name;
    private String code;
    private ArrayList<String> currencies;
    private String region;
    private ArrayList<String> bordering = new ArrayList<>();

    Country(String n, String c, ArrayList<String> cu, String r) {   //, ArrayList<String> b
        this.name = n;
        this.code = c;
        this.currencies = cu;
        this.region = r;
        //this.bordering = b;
    }

    public String getName() { return this.name; }
    public String getCode() { return this.code; }
    public ArrayList<String> getCurrencies() { return this.currencies; }
    public String getRegion() { return this.region; }
    public ArrayList<String> getBordering() { return this.bordering; }

    public void setName(String n) { this.name = n; }
    public void setCode(String c) { this.name = c; }
    public void setCurrencies(ArrayList<String> cu) { this.currencies = cu; }
    public void setRegion(String r) { this.region = r; }
    public void setBordering(ArrayList<String> b) { this.bordering = b; }

    public void addToCurrencies(String c) { this.currencies.add(c); }
    public void clearcurrencies() { this.currencies.clear(); }

    public void addToBordering(String c) { this.bordering.add(c); }
    public void clearBordering() { this.bordering.clear(); }
}