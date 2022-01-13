PHONY: docs graphs
graphs:
	for file in $$(ls docs/*.dot); do \
		echo $${file}; \
		dot2tex --autosize --figonly --texmode raw $${file} > $$(echo $${file} | cut -d'.' -f1).tex; \
	done; 

docs: graphs
	cd docs; xelatex -shell-escape doc.tex && xelatex -shell-escape doc.tex

