//\t([0-9]+)\t([0-9]+)\t([0-9]+)\t([^\t]+)\t([0-9]+)
//{"siruta":"$1","tip":"$2","superior":"$3","nume":"$4","judet":"$5"},
//https://data.gov.ro/dataset/siruta-registrul-unitatilor-administrativ-teritoriale-ale-romaniei
//http://colectaredate.insse.ro/senin/classifications.htm?selectedClassification=SIRUTA_S1_2015&action=download

//[ŢÞ] Ț

//[Ã]  Ă

//[ªŞ] Ș

var temp = {};
var tipuri={
	"40":"Judet,municipiul Bucuresti",
	"1:":"Municipiu  resedinta de judet, municipiul Bucuresti",
	"2": "Oras ce apartine de judet , altele decit  resedinta de judet",
	"3":"Comuna",
	"4":"Municipiu,altele decit resedinta de judet",
	"5":"Oras resedinta de judet",
	"6":"Sectoarele municipiului Bucuresti",
	"9":"Localitate  componenta ,  resedinta de  municipiu",
	"10":"Alte localitati ale municipiului",
	"11":"Sat ce apartine de  municipiu",
	"17":"Localitate componenta resedinta a orasului",
	"18":"Localitati  componente ale orasului altele decit resedinta de oras",
	"19":"Sate subordonate unui oras",
	"22":"Sat resedinta de comuna",
	"23":"Sat ce apartine de comuna, altele decit resedinta de comuna"
};

for (var i = 0; i < jsonInitial.length; i++) {

}





