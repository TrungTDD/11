package com.hackathon.smessage.utils;

import android.util.Patterns;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */

public class PhoneNumberUtils {

    public static String format(String address){
        String phone = address;

        for(int i = 0; i < m_InternationalTelephoneCountryCodesForTextMessages.length; i++){
            String countryCode = "+" + m_InternationalTelephoneCountryCodesForTextMessages[i];
            if(address.contains(countryCode)){
                phone = "0" + address.substring(countryCode.length());
                break;
            }
        }

        //remove all character that is not number : often is contact
        phone = phone.replaceAll("[^\\d.]", "");
        if(phone.isEmpty()){
            phone = address;
        }
        return phone;
    }

    public static boolean isValid(String number)
    {
        return Patterns.PHONE.matcher(number).matches();
    }

    private static final int[] m_InternationalTelephoneCountryCodesForTextMessages = {
            1, //North America
            269, //Mayotte, Comoros Is
            501, //Belize	690	Tokelau
            20, //Egypt
            27,	//South Africa
            502, //Guatemala
            691, //F.S. Micronesia
            212, //Morocco
            290, //Saint Helena
            503, //El Salvador
            692, //Marshall Islands
            213, //Algeria
            291, //	Eritrea
            504, //	Honduras
            7, //	Russia, Kazakhstan
            216, //	Tunisia
            297, //	Aruba
            505, //	Nicaragua
            800, //	Int'l Freephone
            218, //	Libya
            298, //	Faroe Islands
            506, //	Costa Rica
            81, //	Japan
            220, //	Gambia
            299, //	Greenland
            507, //	Panama
            82, //	Korea (South)
            221, //	Senegal
            30, //	Greece
            508, //	St Pierre & Miquelon
            84, //	Viet Nam
            222, //	Mauritania
            31, //	Netherlands
            509, //	Haiti
            850, //	DPR Korea (North)
            223, //	Mali
            32, //	Belgium
            51, //	Peru
            224, //	Guinea
            33, //	France
            52, //	Mexico
            852, //	Hong Kong
            225, //	Ivory Coast
            34, //	Spain
            53, //	Cuba
            853, //	Macau
            226, //	Burkina Faso
            350, //	Gibraltar
            54, //	Argentina
            855, //	Cambodia
            227, //	Niger
            351, //	Portugal
            55, //	Brazil
            856, //	Laos
            228, //	Togo
            352, //	Luxembourg
            56, //	Chile
            86, //	(People's Rep.) China
            229, //	Benin
            353, //	Ireland
            57, //	Colombia
            870, //	Inmarsat SNAC
            230, //Mauritius
            354, //	Iceland
            58, //	Venezuela
            871, //	Inmarsat (Atl-East)
            231, //	Liberia
            355, //	Albania
            590, //	Guadeloupe
            872, //	Inmarsat (Pacific)
            232, //	Sierra Leone
            356, //	Malta
            591, //	Bolivia
            873, //	Inmarsat (Indian O.)
            233, //	Ghana
            357, //	Cyprus
            592, //	Guyana
            874, //	Inmarsat (Atl-West)
            234, //	Nigeria
            358, //	Finland
            593, //	Ecuador
            880, //	Bangladesh
            235, //	Chad
            359, //	Bulgaria
            594, //	Guiana (French)
            881, //	Satellite services
            236, //	Central African Rep.
            36, //	Hungary	595	Paraguay
            886, //	Taiwan/"reserved"
            237, //	Cameroon
            370, //	Lithuania
            596, //	Martinique
            90, //	Turkey
            238, //	Cape Verde
            371, //	Latvia
            597, //	Suriname
            91, //	India
            239, //	Sao Tome & Principe
            372, //	Estonia
            598, //	Uruguay
            92, //	Pakistan
            240, //	Equatorial Guinea
            373, //	Moldova
            599, //	Netherlands Antilles
            93, //	Afghanistan
            241, //	Gabon
            374, //	Armenia
            60, //	Malaysia
            94, //	Sri Lanka
            242, //	Congo
            375, //	Belarus
            61, //	Australia
            95, //	Myanmar (Burma)
            243, //	Zaire
            376, //	Andorra
            62, //	Indonesia
            960, //	Maldives
            244, //	Angola
            377, //	Monaco
            63, //	Philippines
            961, //	Lebanon
            245, //	Guinea-Bissau
            378, //	San Marino
            64, //	New Zealand
            962, //	Jordan
            246, //	Diego Garcia
            379, //	Vatican City (use +39)
            65, //	Singapore
            963, //	Syria
            247, //	Ascension
            380, //	Ukraine
            66, //	Thailand
            964, //	Iraq
            248, //Seychelles
            381, //	Yugoslavia
            670, //	East Timor
            965, //	Kuwait
            249, //	Sudan
            385, //	Croatia
            966, //	Saudi Arabia
            250, //	Rwanda
            386, //	Slovenia
            672, //	Australian Ext. Terr.
            967, //	Yemen
            251, //	Ethiopia
            387, //	Bosnia - Herzegovina
            673, //	Brunei Darussalam
            968, //	Oman
            252, //	Somalia
            389, //	(FYR) Macedonia
            674, //	Nauru
            970, //	Palestine
            253, //	Djibouti
            39, //	Italy
            675, //	Papua New Guinea
            971, //	United Arab Emirates
            254, //	Kenya
            40, //	Romania
            676, //	Tonga
            972, //	Israel
            255, //	Tanzania
            41, //	Switzerland, (Liecht.)
            677, //	Solomon Islands
            973, //	Bahrain
            256, //	Uganda
            678, //	Vanuatu
            974, //	Qatar
            257, //	Burundi
            420, //	Czech Republic
            679, //	Fiji
            975, //	Bhutan
            258, //	Mozambique
            421, //	Slovakia
            680, //	Palau
            976, //	Mongolia
            260, //	Zambia
            423, //	Liechtenstein
            681, //	Wallis and Futuna
            977, //	Nepal
            261, //	Madagascar
            43, //	Austria
            682, //	Cook Islands
            98, //	Iran
            262, //	Reunion Island
            44, //	United Kingdom
            683, //	Niue
            992, //	Tajikistan
            263, //	Zimbabwe
            45, //	Denmark
            684, //	American Samoa
            993, //	Turkmenistan
            264, //Namibia
            46, //	Sweden
            685, //	Western Samoa
            994, //	Azerbaijan
            265, //	Malawi
            47, //	Norway
            686, //	Kiribati
            995, //	Rep. of Georgia
            266, //	Lesotho
            48, //	Poland
            687, //	New Caledonia
            996, //	Kyrgyz Republic
            267, //	Botswana
            49, //	Germany
            688, //	Tuvalu
            997, //	Kazakhstan
            268, //	Swaziland
            500, //Falkland Islands
            689, //	French Polynesia
            998, //	Uzbekistan
    };
}