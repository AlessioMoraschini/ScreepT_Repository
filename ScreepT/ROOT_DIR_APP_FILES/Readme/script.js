/*
 * ScreepT guide: collapsible index and chapters (accordions), live search with filter, reading progress,
 * back to top, light/dark switch, title anchors.
 * Plain ES5 and no libraries, so it runs in the embedded viewer as well as in any browser.
 * The guide stays fully readable without it.
 */
(function () {
	'use strict';

	var STORAGE_KEY = 'screeptGuideTheme';
	var ANIMATION_MS = 380;

	////////// small helpers //////////

	function each(list, fn) {
		for (var i = 0; i < list.length; i++) {
			fn(list[i], i);
		}
	}

	function make(tag, className, text) {
		var el = document.createElement(tag);
		if (className) {
			el.className = className;
		}
		if (text) {
			el.appendChild(document.createTextNode(text));
		}
		return el;
	}

	function hasClass(el, name) {
		return (' ' + el.className + ' ').indexOf(' ' + name + ' ') >= 0;
	}

	function addClass(el, name) {
		if (!hasClass(el, name)) {
			el.className = (el.className ? el.className + ' ' : '') + name;
		}
	}

	function removeClass(el, name) {
		el.className = (' ' + el.className + ' ').replace(' ' + name + ' ', ' ').replace(/^\s+|\s+$/g, '');
	}

	function setClass(el, name, on) {
		if (on) {
			addClass(el, name);
		} else {
			removeClass(el, name);
		}
	}

	function closestWithClass(el, name) {
		while (el && el !== document.body) {
			if (el.nodeType === 1 && hasClass(el, name)) {
				return el;
			}
			el = el.parentNode;
		}
		return null;
	}

	function escapeRegExp(text) {
		return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
	}

	function storageGet(key) {
		try {
			return window.localStorage.getItem(key);
		} catch (e) {
			return null;
		}
	}

	function storageSet(key, value) {
		try {
			window.localStorage.setItem(key, value);
		} catch (e) { /* no memory, no problem */ }
	}

	////////// theme //////////

	function readTheme() {
		var saved = storageGet(STORAGE_KEY);
		if (saved) {
			return saved;
		}
		try {
			if (window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches) {
				return 'light';
			}
		} catch (e) { /* old engine */ }
		return 'dark';
	}

	function applyTheme(theme, button) {
		var light = theme === 'light';
		setClass(document.body, 'light', light);
		if (button) {
			button.innerHTML = light ? '☾' : '☀';
			button.title = light ? 'Switch to the dark theme' : 'Switch to the light theme';
		}
	}

	////////// accordion //////////

	var accordions = [];

	function Accordion(title, body, open) {
		this.title = title;
		this.body = body;
		this.open = open;
		this.userOpen = open;
		this.timer = null;
		var self = this;

		addClass(title, 'accTitle');
		title.setAttribute('role', 'button');
		title.setAttribute('tabindex', '0');
		var chevron = make('span', 'accChevron');
		title.insertBefore(chevron, title.firstChild);

		addClass(body, 'accBody');
		this.apply(false);

		title.onclick = function (e) {
			var target = e.target || e.srcElement;
			if (target && target.tagName === 'A') {
				return; // a link inside the title keeps working
			}
			self.toggle();
		};
		title.onkeydown = function (e) {
			var code = e.keyCode || e.which;
			if (code === 13 || code === 32) {
				self.toggle();
				if (e.preventDefault) {
					e.preventDefault();
				}
			}
		};
	}

	Accordion.prototype.apply = function (animate) {
		var body = this.body;
		var self = this;
		if (this.timer) {
			clearTimeout(this.timer);
			this.timer = null;
		}
		this.title.setAttribute('aria-expanded', this.open ? 'true' : 'false');
		setClass(this.title, 'collapsed', !this.open);

		if (!animate) {
			body.style.maxHeight = this.open ? 'none' : '0px';
			body.style.opacity = this.open ? '1' : '0';
			body.style.visibility = this.open ? 'visible' : 'hidden';
			return;
		}
		if (this.open) {
			body.style.visibility = 'visible';
			body.style.maxHeight = body.scrollHeight + 'px';
			body.style.opacity = '1';
			this.timer = setTimeout(function () {
				body.style.maxHeight = 'none'; // free again: the content may change (search)
				self.timer = null;
			}, ANIMATION_MS + 40);
		} else {
			body.style.maxHeight = body.scrollHeight + 'px';
			void body.offsetHeight; // start point of the transition
			body.style.maxHeight = '0px';
			body.style.opacity = '0';
			this.timer = setTimeout(function () {
				body.style.visibility = 'hidden';
				self.timer = null;
			}, ANIMATION_MS);
		}
	};

	Accordion.prototype.setOpen = function (open, animate, remember) {
		if (remember) {
			this.userOpen = open;
		}
		if (this.open === open) {
			return;
		}
		this.open = open;
		this.apply(animate);
	};

	Accordion.prototype.toggle = function () {
		this.setOpen(!this.open, true, true);
	};

	function accordionOf(el) {
		for (var i = 0; i < accordions.length; i++) {
			if (accordions[i].body === el || accordions[i].body.contains(el)) {
				return accordions[i];
			}
		}
		return null;
	}

	// opens (without animation) every chapter that contains the element: used by the links of the index
	function revealElement(el) {
		for (var i = 0; i < accordions.length; i++) {
			if (accordions[i].body.contains(el)) {
				accordions[i].setOpen(true, false, true);
			}
		}
	}

	function buildAccordions() {
		var index = document.getElementById('indexList');
		var chapters = [];
		var titles = document.querySelectorAll('h1.sectionTitle');
		each(titles, function (title) {
			var container = title.parentNode;
			var isIndex = container === index;
			var body = make('div');
			// everything that follows the title, up to the end of the container, goes into the collapsible body
			while (title.nextSibling) {
				body.appendChild(title.nextSibling);
			}
			container.appendChild(body);
			var accordion = new Accordion(title, body, true);
			accordion.isIndex = isIndex;
			accordion.container = container;
			accordions.push(accordion);
			if (!isIndex) {
				chapters.push(accordion);
			}
		});
		return chapters;
	}

	////////// sections (units of the search) //////////

	var sections = [];

	function isSectionStart(node) {
		if (node.nodeType !== 1) {
			return false;
		}
		if (node.tagName === 'H2') {
			return true;
		}
		return node.tagName === 'LI' && !!node.querySelector('.subMenuthirdLevel');
	}

	function buildSections(chapters) {
		each(chapters, function (chapter) {
			var body = chapter.body;
			var children = [];
			each(body.childNodes, function (n) {
				children.push(n);
			});
			var current = null;
			var currentStartsWithContent = false;
			each(children, function (node) {
				if (!current || isSectionStart(node)) {
					if (current && !currentStartsWithContent) {
						// nothing but separators so far: keep them with the next section
					}
					current = make('div', 'guideSection');
					body.insertBefore(current, node);
					sections.push({ el: current, chapter: chapter, hidden: false });
				}
				current.appendChild(node);
				if (node.nodeType === 1 && node.tagName !== 'HR' && node.tagName !== 'BR') {
					currentStartsWithContent = true;
				}
			});
		});
	}

	////////// search //////////

	var search = {
		input: null,
		navInput: null,
		inputs: [],
		counters: [],
		toggles: [],
		options: { matchCase: false, wholeWord: false, regex: false, allWords: true },
		marks: [],
		current: -1,
		timer: null,
		active: false
	};

	function buildMatcher(query) {
		// returns { test: function(text), highlight: RegExp } or null (empty query) or { invalid: true }
		if (!query) {
			return null;
		}
		var o = search.options;
		var flags = o.matchCase ? 'g' : 'gi';
		var terms;
		if (o.regex) {
			terms = [query];
		} else if (o.allWords) {
			terms = query.split(/\s+/).filter(function (t) { return t.length > 0; });
		} else {
			terms = [query];
		}
		var patterns = [];
		for (var i = 0; i < terms.length; i++) {
			var source = o.regex ? terms[i] : escapeRegExp(terms[i]);
			if (o.wholeWord) {
				source = '\\b(?:' + source + ')\\b';
			}
			try {
				patterns.push(new RegExp(source, flags));
			} catch (e) {
				return { invalid: true };
			}
		}
		var all = [];
		for (var j = 0; j < patterns.length; j++) {
			all.push('(?:' + patterns[j].source + ')');
		}
		var highlight;
		try {
			highlight = new RegExp(all.join('|'), flags);
		} catch (e2) {
			return { invalid: true };
		}
		return {
			highlight: highlight,
			test: function (text) {
				for (var k = 0; k < patterns.length; k++) {
					patterns[k].lastIndex = 0;
					if (!patterns[k].test(text)) {
						return false;
					}
				}
				return true;
			}
		};
	}

	function clearMarks() {
		each(document.querySelectorAll('mark.hit'), function (mark) {
			var parent = mark.parentNode;
			if (!parent) {
				return;
			}
			parent.replaceChild(document.createTextNode(mark.textContent), mark);
			parent.normalize();
		});
		search.marks = [];
		search.current = -1;
	}

	function highlightIn(root, regex) {
		var walker = document.createTreeWalker(root, 4 /* NodeFilter.SHOW_TEXT */, null, false);
		var nodes = [];
		while (walker.nextNode()) {
			var node = walker.currentNode;
			var parentTag = node.parentNode ? node.parentNode.tagName : '';
			if (parentTag !== 'SCRIPT' && parentTag !== 'STYLE' && node.nodeValue.replace(/\s+/g, '').length > 0) {
				nodes.push(node);
			}
		}
		each(nodes, function (node) {
			var text = node.nodeValue;
			regex.lastIndex = 0;
			var last = 0;
			var fragment = null;
			var m;
			while ((m = regex.exec(text)) !== null) {
				if (m[0].length === 0) {
					regex.lastIndex++;
					continue;
				}
				fragment = fragment || document.createDocumentFragment();
				if (m.index > last) {
					fragment.appendChild(document.createTextNode(text.substring(last, m.index)));
				}
				var mark = make('mark', 'hit', m[0]);
				fragment.appendChild(mark);
				search.marks.push(mark);
				last = m.index + m[0].length;
			}
			if (fragment) {
				if (last < text.length) {
					fragment.appendChild(document.createTextNode(text.substring(last)));
				}
				node.parentNode.replaceChild(fragment, node);
			}
		});
	}

	function setCounter(text, state) {
		each(search.counters, function (counter) {
			counter.innerHTML = text;
			counter.className = 'guideCounter' + (state ? ' ' + state : '');
		});
		each(search.inputs, function (input) {
			setClass(input, 'bad', state === 'bad');
		});
	}

	function setQuery(value) {
		each(search.inputs, function (input) {
			if (input.value !== value) {
				input.value = value;
			}
		});
	}

	function targetOf(link) {
		var href = link.getAttribute('href') || '';
		return href.charAt(0) === '#' ? document.getElementById(href.substring(1)) : null;
	}

	// the entries of the index follow the sections: only the ones whose section matches stay
	function filterIndex(visibleChapters) {
		filterIndexIn(document.getElementById('indexList'), visibleChapters);
		filterIndexIn(document.getElementById('guideNavTree'), visibleChapters);
	}

	function filterIndexIn(list, visibleChapters) {
		if (!list) {
			return;
		}
		each(list.querySelectorAll('li'), function (li) {
			li.style.display = '';
		});
		if (!search.active) {
			return;
		}
		var items = list.querySelectorAll('li');
		// deepest entries first, so a parent can look at its children
		for (var i = items.length - 1; i >= 0; i--) {
			var li = items[i];
			var link = li.querySelector('a[href^="#"]');
			var visible = false;
			if (link) {
				var target = targetOf(link);
				var section = target ? closestWithClass(target, 'guideSection') : null;
				if (section) {
					visible = section.style.display !== 'none';
				} else if (target) {
					for (var c = 0; c < visibleChapters.length; c++) {
						if (visibleChapters[c].container === target || visibleChapters[c].container.contains(target)) {
							visible = true;
						}
					}
				}
			}
			if (!visible) {
				var children = li.querySelectorAll('li');
				for (var k = 0; k < children.length; k++) {
					if (children[k].style.display !== 'none') {
						visible = true;
						break;
					}
				}
			}
			li.style.display = visible ? '' : 'none';
		}
	}

	function runSearch() {
		var query = search.input.value;
		clearMarks();
		var matcher = buildMatcher(query);
		search.active = !!matcher && !matcher.invalid;
		setClass(document.body, 'searching', search.active);

		if (matcher && matcher.invalid) {
			setCounter('Invalid expression', 'bad');
			showAll();
			return;
		}
		if (!matcher) {
			setCounter('', '');
			showAll();
			return;
		}

		var visibleSections = 0;
		var visibleChapters = [];
		each(sections, function (section) {
			var hit = matcher.test(section.el.textContent);
			section.el.style.display = hit ? '' : 'none';
			if (hit) {
				visibleSections++;
			}
		});
		each(accordions, function (accordion) {
			if (accordion.isIndex) {
				return;
			}
			var any = false;
			each(sections, function (section) {
				if (section.chapter === accordion && section.el.style.display !== 'none') {
					any = true;
				}
			});
			accordion.container.style.display = any ? '' : 'none';
			if (any) {
				visibleChapters.push(accordion);
				accordion.setOpen(true, false, false); // matches must be visible, the choice of the reader comes back later
			}
		});

		// the highlight works on the sections that stay and on the index
		each(sections, function (section) {
			if (section.el.style.display !== 'none') {
				highlightIn(section.el, matcher.highlight);
			}
		});
		filterIndex(visibleChapters);

		if (visibleSections === 0) {
			setCounter('No results', 'bad');
			return;
		}
		setCounter(search.marks.length + ' matches in ' + visibleSections + ' section' + (visibleSections === 1 ? '' : 's'), 'ok');
		if (search.marks.length > 0) {
			goToMark(nearestMark(), true);
		}
	}

	function showAll() {
		each(sections, function (section) {
			section.el.style.display = '';
		});
		each(accordions, function (accordion) {
			if (!accordion.isIndex) {
				accordion.container.style.display = '';
			}
			accordion.setOpen(accordion.userOpen, false, false);
		});
		filterIndex([]);
	}

	// first match from the top of the window down: the closest to where the reader is, or the first of all when none follows
	function nearestMark() {
		for (var i = 0; i < search.marks.length; i++) {
			if (search.marks[i].getBoundingClientRect().top >= 110) {
				return i;
			}
		}
		return 0;
	}

	function goToMark(index, scroll) {
		if (search.marks.length === 0) {
			return;
		}
		var total = search.marks.length;
		var wrapNote = '';
		if (index >= total) {
			wrapNote = ' &middot; \u21bb from the first';
		} else if (index < 0) {
			wrapNote = ' &middot; \u21bb from the last';
		}
		if (search.current >= 0 && search.marks[search.current]) {
			removeClass(search.marks[search.current], 'cur');
		}
		search.current = (index + total) % total;
		var mark = search.marks[search.current];
		addClass(mark, 'cur');
		if (scroll !== false) {
			var rect = mark.getBoundingClientRect();
			var height = window.innerHeight || document.documentElement.clientHeight;
			if (rect.top < 110 || rect.bottom > height - 40) {
				window.scrollTo(0, (window.pageYOffset || document.documentElement.scrollTop || 0) + rect.top - height / 3);
			}
		}
		var sectionCount = 0;
		each(sections, function (s) {
			if (s.el.style.display !== 'none') {
				sectionCount++;
			}
		});
		setCounter((search.current + 1) + ' / ' + total + ' &middot; ' + sectionCount + ' section' + (sectionCount === 1 ? '' : 's') + wrapNote, 'ok');
	}

	function scheduleSearch() {
		if (search.timer) {
			clearTimeout(search.timer);
		}
		search.timer = setTimeout(runSearch, 140);
	}

	// search field, switches, counter and match buttons: the same set is used in the top toolbar and in the side index
	function buildControls(parent, compact) {
		var row = make('div', 'guideRow');
		var box = make('div', 'guideSearchBox');
		var input = make('input');
		input.type = 'text';
		if (!compact) {
			input.id = 'guideSearch';
		}
		input.setAttribute('placeholder', compact ? 'Filter the guide...' : 'Search the guide...  (press / to jump here)');
		input.setAttribute('autocomplete', 'off');
		input.setAttribute('spellcheck', 'false');
		box.appendChild(input);
		var clear = make('span', 'guideClear', '\u00d7');
		clear.title = 'Clear the search (Esc)';
		box.appendChild(clear);
		row.appendChild(box);
		search.inputs.push(input);
		if (compact) {
			search.navInput = input;
		} else {
			search.input = input;
		}

		function refreshToggles() {
			each(search.toggles, function (t) {
				var on = search.options[t.key];
				setClass(t.button, 'on', on);
				t.button.setAttribute('aria-pressed', on ? 'true' : 'false');
			});
		}

		function addToggle(label, title, key, extraClass) {
			var button = make('button', 'guideToggle' + (extraClass ? ' ' + extraClass : ''));
			button.type = 'button';
			button.innerHTML = label;
			button.title = title;
			search.toggles.push({ key: key, button: button });
			button.onclick = function () {
				search.options[key] = !search.options[key];
				refreshToggles();
				runSearch();
				input.focus();
			};
			row.appendChild(button);
		}
		addToggle('Aa', 'Match case', 'matchCase');
		addToggle('<span class="wordIcon">ab</span>', 'Whole word', 'wholeWord');
		addToggle('.*', 'Regular expression', 'regex');
		addToggle('All&nbsp;words', 'All the words must be in the same section, in any order (plain text only)', 'allWords', 'wide');

		var prev = make('button', 'guideStep', '\u2191');
		prev.type = 'button';
		prev.title = 'Previous match (Shift+Enter): from the first it goes to the last';
		var next = make('button', 'guideStep', '\u2193');
		next.type = 'button';
		next.title = 'Next match (Enter): from the last it starts again from the first';
		row.appendChild(prev);
		row.appendChild(next);
		parent.appendChild(row);
		refreshToggles();

		var info = make('div', 'guideRow guideInfo');
		var counter = make('span', 'guideCounter');
		info.appendChild(counter);
		search.counters.push(counter);
		parent.appendChild(info);

		input.oninput = function () {
			setQuery(input.value);
			scheduleSearch();
		};
		input.onkeydown = function (e) {
			var code = e.keyCode || e.which;
			if (code === 13) {
				goToMark(search.current + (e.shiftKey ? -1 : 1), true);
				if (e.preventDefault) {
					e.preventDefault();
				}
			} else if (code === 27) {
				setQuery('');
				runSearch();
			}
		};
		clear.onclick = function () {
			setQuery('');
			runSearch();
			input.focus();
		};
		prev.onclick = function () {
			goToMark(search.current - 1, true);
		};
		next.onclick = function () {
			goToMark(search.current + 1, true);
		};
		return info;
	}

	function buildTools() {
		var index = document.getElementById('indexList');
		if (!index) {
			return;
		}
		var tools = make('div');
		tools.id = 'guideTools';
		var info = buildControls(tools, false);

		var expand = make('button', 'guideLink', 'Expand all');
		var collapse = make('button', 'guideLink', 'Collapse all');
		expand.type = 'button';
		collapse.type = 'button';
		info.appendChild(expand);
		info.appendChild(collapse);
		expand.onclick = function () {
			each(accordions, function (a) {
				a.setOpen(true, true, true);
			});
		};
		collapse.onclick = function () {
			each(accordions, function (a) {
				if (!a.isIndex) {
					a.setOpen(false, true, true);
				}
			});
		};
		index.parentNode.insertBefore(tools, index);

		// "/" jumps to the search, as in most documentation sites
		document.onkeydown = function (e) {
			var code = e.keyCode || e.which;
			var tag = (e.target || e.srcElement || {}).tagName;
			if (code === 191 && !e.ctrlKey && !e.altKey && !e.metaKey && tag !== 'INPUT' && tag !== 'TEXTAREA') {
				(nav.shown && search.navInput ? search.navInput : search.input).focus();
				if (e.preventDefault) {
					e.preventDefault();
				}
			}
		};
	}

	////////// side index //////////

	var nav = { el: null, tree: null, shown: false, hover: false, timer: null, spyTimer: null, min: 380, idle: 4000 };

	function scrollTopNow() {
		return window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0;
	}

	function buildNav() {
		var indexAccordion = null;
		each(accordions, function (a) {
			if (a.isIndex) {
				indexAccordion = a;
			}
		});
		if (!indexAccordion) {
			return;
		}
		var el = make('div');
		el.id = 'guideNav';
		var head = make('div', 'navHead');
		head.appendChild(make('span', 'navTitle', 'Guide'));
		var top = make('span', 'navTop', '\u2191 top');
		top.title = 'Back to the top of the guide';
		top.onclick = function () {
			window.scrollTo(0, 0);
		};
		head.appendChild(top);
		el.appendChild(head);
		buildControls(el, true);

		// the tree is a copy of the index of the page: same chapters, same links
		var tree = make('div');
		tree.id = 'guideNavTree';
		var copy = indexAccordion.body.cloneNode(true);
		copy.removeAttribute('style');
		copy.className = '';
		each(copy.querySelectorAll('[id]'), function (n) {
			n.removeAttribute('id');
		});
		tree.appendChild(copy);
		el.appendChild(tree);
		document.body.appendChild(el);
		nav.el = el;
		nav.tree = tree;

		el.onmouseover = function () {
			nav.hover = true;
		};
		el.onmouseout = function (e) {
			var to = e.relatedTarget || e.toElement;
			if (!to || !el.contains(to)) {
				nav.hover = false;
				navActivity();
			}
		};
	}

	// the tree starts under the search controls, whatever their height
	function layoutNav() {
		var infos = nav.el.querySelectorAll('.guideInfo');
		var last = infos.length ? infos[infos.length - 1] : null;
		if (last) {
			nav.tree.style.top = (last.offsetTop + last.offsetHeight + 8) + 'px';
		}
	}

	function navAllowed() {
		return scrollTopNow() > nav.min;
	}

	function hideNav(force) {
		if (!nav.el) {
			return;
		}
		var busy = nav.hover || (document.activeElement && nav.el.contains(document.activeElement));
		if (busy && !force) {
			navActivity();
			return;
		}
		nav.shown = false;
		removeClass(nav.el, 'shown');
	}

	// the index appears when the reader is down the page and moves, and goes away after 4 seconds without movement
	function navActivity() {
		if (!nav.el) {
			return;
		}
		if (!navAllowed()) {
			hideNav(true);
			return;
		}
		if (!nav.shown) {
			nav.shown = true;
			addClass(nav.el, 'shown');
			layoutNav();
			scheduleSpy();
		}
		if (nav.timer) {
			clearTimeout(nav.timer);
		}
		nav.timer = setTimeout(function () {
			hideNav(false);
		}, nav.idle);
	}

	// the entry of the section that is on screen is highlighted
	function updateSpy() {
		if (!nav.el || !nav.shown) {
			return;
		}
		var limit = scrollTopNow() + 150;
		var best = null;
		var links = nav.tree.querySelectorAll('a[href^="#"]');
		each(links, function (a) {
			removeClass(a, 'active');
			var target = targetOf(a);
			if (!target || target.offsetParent === null) {
				return;
			}
			var top = target.getBoundingClientRect().top + scrollTopNow();
			if (top <= limit) {
				best = a;
			}
		});
		if (best) {
			addClass(best, 'active');
			var y = best.getBoundingClientRect().top - nav.tree.getBoundingClientRect().top;
			if (y < 20 || y > nav.tree.clientHeight - 40) {
				nav.tree.scrollTop = nav.tree.scrollTop + y - nav.tree.clientHeight / 3;
			}
		}
	}

	function scheduleSpy() {
		if (nav.spyTimer) {
			return;
		}
		nav.spyTimer = setTimeout(function () {
			nav.spyTimer = null;
			updateSpy();
		}, 120);
	}

	////////// links to a closed chapter //////////

	function wireLinks() {
		each(document.querySelectorAll('a[href^="#"]'), function (link) {
			if (hasClass(link, 'anchorMark')) {
				return;
			}
			link.addEventListener('click', function () {
				var target = targetOf(link);
				if (target) {
					var section = closestWithClass(target, 'guideSection');
					if (section && section.style.display === 'none') {
						setQuery('');
						runSearch();
					}
					revealElement(target);
				}
			}, false);
		});
		if (window.location.hash && window.location.hash.length > 1) {
			var initial = document.getElementById(window.location.hash.substring(1));
			if (initial) {
				revealElement(initial);
				setTimeout(function () {
					initial.scrollIntoView(true);
				}, 30);
			}
		}
	}

	////////// init //////////

	function init() {
		var body = document.body;

		var bar = make('div');
		bar.id = 'guideProgress';
		body.appendChild(bar);

		var themeButton = make('div', 'guideFab');
		themeButton.id = 'guideTheme';
		body.appendChild(themeButton);
		var theme = readTheme();
		applyTheme(theme, themeButton);
		themeButton.onclick = function () {
			theme = theme === 'light' ? 'dark' : 'light';
			applyTheme(theme, themeButton);
			storageSet(STORAGE_KEY, theme);
		};

		var topButton = make('div', 'guideFab');
		topButton.id = 'guideTop';
		topButton.innerHTML = '↑';
		topButton.title = 'Back to top';
		topButton.onclick = function () {
			window.scrollTo(0, 0);
		};
		body.appendChild(topButton);

		// anchor mark on the titles that have an id (done before the accordions move the titles)
		each(document.querySelectorAll('h1[id], h2[id]'), function (title) {
			if (title.id === 'indexTitle') {
				return;
			}
			var mark = make('a', 'anchorMark', '#');
			mark.href = '#' + title.id;
			mark.title = 'Link to this section';
			title.appendChild(mark);
		});

		var chapters = buildAccordions();
		buildSections(chapters);
		buildTools();
		buildNav();
		wireLinks();

		function onScroll() {
			var doc = document.documentElement;
			var top = scrollTopNow();
			var height = (doc.scrollHeight || body.scrollHeight) - (window.innerHeight || doc.clientHeight);
			bar.style.width = (height > 0 ? Math.min(100, top * 100 / height) : 0) + '%';
			setClass(topButton, 'shown', top > 500);
			navActivity();
			scheduleSpy();
		}
		window.onscroll = onScroll;
		document.addEventListener('mousemove', navActivity, false);
		document.addEventListener('mousedown', navActivity, false);
		document.addEventListener('touchstart', navActivity, false);
		document.addEventListener('keydown', navActivity, false);
		onScroll();
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', init, false);
	} else {
		init();
	}
})();

/*
 * Animated hero: pixel rain behind the title (ScreepT teal, like the launcher). ES5, canvas only,
 * stops when the hero is out of view or the page is hidden; without canvas the title stays as it is.
 */
(function () {
	'use strict';

	var CELL = 8;
	var canvas, ctx, drops, cols, rows, timer = null, block;

	function isLight() {
		return /(^|\s)light(\s|$)/.test(document.body.className);
	}

	function resize() {
		var w = block.offsetWidth, h = block.offsetHeight;
		canvas.width = w;
		canvas.height = h;
		cols = Math.ceil(w / CELL);
		rows = Math.ceil(h / CELL);
		drops = [];
		for (var c = 0; c < cols; c++) {
			drops.push({ y: -Math.random() * rows, speed: 0.25 + Math.random() * 0.55, len: 5 + Math.floor(Math.random() * 9) });
		}
	}

	function frame() {
		if (document.hidden || (window.pageYOffset || 0) > block.offsetHeight + 40) {
			return;
		}
		ctx.clearRect(0, 0, canvas.width, canvas.height);
		var rgb = isLight() ? '31,127,118' : '46,196,182';
		for (var c = 0; c < cols; c++) {
			var d = drops[c];
			d.y += d.speed;
			if (d.y - d.len > rows) {
				d.y = -Math.random() * 12;
				d.speed = 0.25 + Math.random() * 0.55;
				d.len = 5 + Math.floor(Math.random() * 9);
			}
			for (var i = 0; i < d.len; i++) {
				var row = Math.floor(d.y) - i;
				if (row < 0 || row >= rows) {
					continue;
				}
				var a = (1 - i / d.len) * (isLight() ? 0.28 : 0.34);
				ctx.fillStyle = 'rgba(' + (i === 0 ? '200,255,248' : rgb) + ',' + a + ')';
				ctx.fillRect(c * CELL, row * CELL, CELL - 2, CELL - 2);
			}
		}
	}

	function start() {
		block = document.getElementById('titleBlock');
		canvas = document.getElementById('heroCanvas');
		if (!block || !canvas || !canvas.getContext) {
			return;
		}
		ctx = canvas.getContext('2d');
		resize();
		window.addEventListener('resize', resize, false);
		timer = window.setInterval(frame, 60);
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', start, false);
	} else {
		start();
	}
})();
