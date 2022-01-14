Phony: clean docs

%.dot.tex: %.dot
	dot2tex --autosize --figonly --texmode raw $< > $@

docs/doc.pdf: docs/doc.tex docs/pipeline.dot.tex docs/program.dot.tex docs/reader.dot.tex
	cd docs; xelatex -shell-escape doc.tex && xelatex -shell-escape doc.tex

clean:
	rm *.m3u8 *.ts docs/*.dot.tex 
