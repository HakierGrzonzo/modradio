Phony: clean docs

%.dot.tex: %.dot
	dot2tex --autosize --figonly --texmode raw $< > $@

docs/doc.pdf: docs/doc.tex docs/pipeline.dot.tex docs/program.dot.tex docs/reader.dot.tex
	cd docs; xelatex -shell-escape doc.tex && xelatex -shell-escape doc.tex

docs/prez.pdf: docs/prez.md
	pandoc docs/prez.md -t beamer -o docs/prez.pdf

clean:
	rm *.m3u8 *.ts docs/*.dot.tex 
