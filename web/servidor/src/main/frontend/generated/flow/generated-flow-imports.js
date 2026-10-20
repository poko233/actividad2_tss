import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/vertical-layout/src/vaadin-vertical-layout.js';
import '@vaadin/app-layout/src/vaadin-app-layout.js';
import '@vaadin/tooltip/src/vaadin-tooltip.js';
import '@vaadin/app-layout/src/vaadin-drawer-toggle.js';
import '@vaadin/tabs/src/vaadin-tab.js';
import '@vaadin/horizontal-layout/src/vaadin-horizontal-layout.js';
import '@vaadin/tabs/src/vaadin-tabs.js';
import '@vaadin/button/src/vaadin-button.js';
import '@vaadin/details/src/vaadin-details.js';
import 'Frontend/generated/jar-resources/disableOnClickFunctions.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/color-global.js';
import '@vaadin/vaadin-lumo-styles/typography-global.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';
import 'Frontend/generated/jar-resources/ReactRouterOutletElement.tsx';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === 'ef9056735503ef15836063d37be8bed816d9ec2f39d4931eab4db018a73b89a8') {
    pending.push(import('./chunks/chunk-a4ee55bcbecfdae3e57e19ddecfac3f21b998598b8dc2537bb8bfd51b13373af.js'));
  }
  if (key === '0d50b5486aac0b0b6bed92b5f62538187878454f282542eb12010ca1ce7d7bf2') {
    pending.push(import('./chunks/chunk-a4ee55bcbecfdae3e57e19ddecfac3f21b998598b8dc2537bb8bfd51b13373af.js'));
  }
  if (key === '8811d7c2ab5d7903ac0d721ddd02c79f605e2b1b7b55e1bb14bede31fc6cac06') {
    pending.push(import('./chunks/chunk-5186e10a4b678cd2b9623a3daa5627b0b1d76847ba576c19cf15d26babd67900.js'));
  }
  if (key === '454eb3d9b9b444a80b8a20f6a55eeeb6a3ab4f74d1abd7694557ff1af82e8c82') {
    pending.push(import('./chunks/chunk-5186e10a4b678cd2b9623a3daa5627b0b1d76847ba576c19cf15d26babd67900.js'));
  }
  if (key === 'eba7582de0afb43b12dbd8faf11aa73bbba79eaf40e7734e34337f6e4884b95c') {
    pending.push(import('./chunks/chunk-fc6cc49422349af0673fa66e48ab28d073448e7d8993bb678d2c02f2d7354880.js'));
  }
  if (key === 'e49da1e99eb4d858b6e7b4424f9bebec3225f732d35b46805e5209685946f182') {
    pending.push(import('./chunks/chunk-5186e10a4b678cd2b9623a3daa5627b0b1d76847ba576c19cf15d26babd67900.js'));
  }
  if (key === '6b532e03523a6611cf2a8400261c0afcf7def2202767e55abeb8ec5a7c768fdf') {
    pending.push(import('./chunks/chunk-d58119422b1b1dff247609aee72a9e608ae388f5e115b017e5479d15b2a00acb.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}