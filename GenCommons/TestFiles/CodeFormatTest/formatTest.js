//public class CodeFormatTest {
//	
//	public static final String codeSourceFilePath = "TestFiles/CodeFormatTest/formatTest.js";
//	public File codeSourceFile;
//	
//	public void init() {		codeSourceFile = new File(codeSourceFilePath);}
//	public void testEclipseDefaultConverter(){
//		String source = FileStuff.readFileAsString(codeSourceFile);
//		String formatted = CustomCodeFormat.googleJavaCodeFormat(source);
//		
//		TestUtils.printHeader("/", 50, "RAW SOURCE CODE - from " + codeSourceFile.getAbsolutePath());
//		TestUtils.printl(source);
//
//		TestUtils.printHeader("/", 50, "FORMATTED SOURCE CODE - from " + codeSourceFile.getAbsolutePath());
//		TestUtils.printl(formatted);
//	}
//}

(function(){function K(e,b){var m={},n;for(n in e)n!==b&&(m[n]=e[n]);if(b in e)for(n in e[b])m[n]=e[b][n];return m}function s(e,b,m,n){var a,s,C,D,f,v,k,w,x,E,F,G,r,t,H,z,A,I,J,p;b=b||{};b=K(b,"html");void 0!==b.wrap_line_length&&0!==parseInt(b.wrap_line_length,10)||void 0===b.max_char||0===parseInt(b.max_char,10)||(b.wrap_line_length=b.max_char);s=void 0===b.indent_inner_html?!1:b.indent_inner_html;C=void 0===b.indent_body_inner_html?!0:b.indent_body_inner_html;D=void 0===b.indent_head_inner_html?
!0:b.indent_head_inner_html;f=void 0===b.indent_size?4:parseInt(b.indent_size,10);v=void 0===b.indent_char?" ":b.indent_char;w=void 0===b.brace_style?"collapse":b.brace_style;k=0===parseInt(b.wrap_line_length,10)?32786:parseInt(b.wrap_line_length||250,10);x=b.unformatted||"a abbr area audio b bdi bdo br button canvas cite code data datalist del dfn em embed i iframe img input ins kbd keygen label map mark math meter noscript object output progress q ruby s samp select small span strong sub sup svg template textarea time u var video wbr text acronym address big dt ins strike tt".split(" ");