function c(e=300,t=200,o="暂无图片"){const n=`
    <svg width="${e}" height="${t}" xmlns="http://www.w3.org/2000/svg">
      <rect width="${e}" height="${t}" fill="#f0f0f0"/>
      <text x="50%" y="50%" font-size="16" fill="#999" text-anchor="middle" dominant-baseline="middle" font-family="Arial">${o}</text>
    </svg>
  `;return`data:image/svg+xml;charset=utf-8,${encodeURIComponent(n)}`}function i(e="U",t=40){const o=["#40c9c6","#36a3eb","#f56c6c","#e6a23c","#67c23a","#909399"],n=e.charCodeAt(0)%o.length,a=o[n],l=`
    <svg width="${t}" height="${t}" xmlns="http://www.w3.org/2000/svg">
      <rect width="${t}" height="${t}" fill="${a}"/>
      <text x="50%" y="50%" font-size="${t*.5}" fill="white" text-anchor="middle" dominant-baseline="middle" font-family="Arial">${e.charAt(0).toUpperCase()}</text>
    </svg>
  `;return`data:image/svg+xml;charset=utf-8,${encodeURIComponent(l)}`}export{c as a,i as g};
