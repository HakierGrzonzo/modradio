Phony: clean docs

%.tex: %.dot
	dot2tex --autosize --figonly --texmode raw $< > $@

docs: docs/doc.tex docs/pipeline.tex docs/program.tex docs/reader.tex
	cd docs; xelatex -shell-escape doc.tex && xelatex -shell-escape doc.tex

clean:
	rm *.m3u8 *.ts docs/program.tex docs/pipeline.tex docs/reader.tex
