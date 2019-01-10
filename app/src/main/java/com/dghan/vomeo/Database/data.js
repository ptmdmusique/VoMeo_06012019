var temp1 = document.getElementsByTagName("th");
var temp2 = document.getElementsByTagName("td");
term = []
definition = []
for( i = 0; i < 100; i++){
	term[i] = temp1[i].outerText;
	definition[i] = temp2[i].outerText;
}
initWordList = {
		terms:		term,
		definitions:	definition,
};
console.log(JSON.stringify(initWordList, null, 2))