const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

/** @type {import('@docusaurus/types').DocusaurusConfig} */
module.exports = {
  title: 'ESCRITO Documentation',
  tagline: 'How to use the Educational SCoRIng TOolkit',
  url: 'https://ltl-ude.github.io',
  baseUrl: '/Escrito-documentation/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',
  organizationName: 'ltl-ude', // Usually your GitHub org/user name.
  projectName: 'Escrito-documentation', // Usually your repo name.
  themeConfig: {
	navbar: {
	    title: 'ESCRITO Documentation',
	    logo: {
		alt: 'UDE signet',
		src: 'img/ude/signet_ude_primary.svg',
	    },
	    //	    items: [
	    //{to: '/docs/01/01', label: 'Lectures', position: 'left'},
	    //{to: '/setup', label: 'Setup', position: 'left'}
	    //	    ],
	},
	footer: {
	    style: 'light',
	    logo: {
		alt: 'Universität Duisburg-Essen Logo',
		src: 'img/ude/logo_claim_negativ.svg',
		href: 'https://www.uni-due.de',
	    },
	    //	    copyright: `
	    //<a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/" target="_blank" rel="noopener noreferrer">
	    //<img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png" />
	    //</a>
	    //<br />
	    //This work is licensed under a 
	    //<a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/" target="_blank" rel="noopener noreferrer">
	    //Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License
	    //</a>`,
	    links: [
{
    title: 'Contact',
},
{
    title: "Source Code",
    items: [
            {
		label: 'GitHub',
		href: 'https://github.com/ltl-ude/escrito'
            }
          ]
}//,
//{
//    title: 'More',
//    items: [
//           {
//		label: 'About',
//		to: 'about/',
//            }
//          ]
//}
      ]
	},
	prism: {
	    theme: lightCodeTheme,
	    darkTheme: darkCodeTheme,
	},
    },
  presets: [
    [
     '@docusaurus/preset-classic',
{
    docs: {
	sidebarPath: require.resolve('./sidebars.js'),
	// Please change this to your repo.
	// editUrl:
	//   'https://github.com/facebook/docusaurus/edit/master/website/',
    },
    blog: {
	showReadingTime: true,
	// Please change this to your repo.
	// editUrl:
	//   'https://github.com/facebook/docusaurus/edit/master/website/blog/',
    },
    theme: {
	customCss: require.resolve('./src/css/custom.css'),
    },
},
     ],
	    ],
      };