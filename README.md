Az alkalmazással részvények aktuális és historikus árát lehet megtekinteni.

API: https://alpaca.markets

Az API-n keresztül az összes amerikai részvény böngészhető

Néhány információ az alkalmazásról:
- Ha az API kulcsok nem helyesek, az alkalmazás nem engedi őket használni
- Alapvetően ha hálózati hiba lép fel, akkor az alkalmazás a hibát kiírja a konzolra.
- A grafikonokon görgővel lehet nagyítani és görgő gomb nyomvatartásával lehet azt mozgatni.
- Az alkalmazás 18-as Java verziót használ
- Az alkalmazás a saját mappájába elmenti a tárolt részvények listáját és az api kulcsokat is json formátumban és indulásnál megpróbálja betölteni azokat.
- A használt tőzsde API-ból nem megbízható adatok is érkeznek, pl.: "stock split" -re korrigálatlanok az adatok, például ezért tűnik úgy, hogy a TSLA értéke ötödére zuhant 2020 közepén, pedig valójában a részvényeket darabolták.
- A grafikonon szakadásokkal van jelölve amikor a tőzsde zárva volt.